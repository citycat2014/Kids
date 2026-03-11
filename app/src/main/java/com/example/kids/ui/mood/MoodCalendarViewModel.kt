package com.example.kids.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.ExerciseRecordEntity
import com.example.kids.data.model.MoodRecordEntity
import com.example.kids.data.repository.ExerciseRecordRepository
import com.example.kids.data.repository.MoodRecordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class KidMood(val value: Int) {
    GOOD(1),
    OK(2),
    BAD(3);

    companion object {
        fun fromInt(value: Int): KidMood? =
            entries.firstOrNull { it.value == value }
    }
}

enum class ExerciseType(val displayName: String) {
    HIIT("HIIT"),
    BADMINTON("羽毛球"),
    PING_PONG("乒乓球"),
    JUMP_ROPE("跳绳"),
    RUNNING("跑步"),
    OTHER("其他")
}

data class ExerciseInfo(
    val id: Long,
    val type: ExerciseType,
    val durationMinutes: Int
)

data class MoodCalendarUiState(
    val kidId: Long = 0L,
    val month: YearMonth = YearMonth.now(),
    val moods: Map<LocalDate, KidMood> = emptyMap(),
    val exercises: Map<LocalDate, List<ExerciseInfo>> = emptyMap()
)

class MoodCalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = KidsDatabase.getInstance(application)
    private val moodRepository = MoodRecordRepository(database.moodRecordDao())
    private val exerciseRepository = ExerciseRecordRepository(database.exerciseRecordDao())

    private val _uiState = MutableStateFlow(MoodCalendarUiState())
    val uiState: StateFlow<MoodCalendarUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun load(kidId: Long) {
        if (kidId == 0L) return
        if (kidId != _uiState.value.kidId) {
            _uiState.value = MoodCalendarUiState(kidId = kidId)
            observeForCurrentMonth()
        } else if (observeJob == null) {
            observeForCurrentMonth()
        }
    }

    private fun observeForCurrentMonth() {
        val state = _uiState.value
        val month = state.month
        val start = month.atDay(1)
        val end = month.atEndOfMonth()

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            // Combine mood and exercise data
            moodRepository.observeMoodForKidInRange(state.kidId, start, end)
                .collect { moodList ->
                    val exercises = exerciseRepository.getExercisesForKidInRange(state.kidId, start, end)

                    // Group exercises by date using mood records
                    val moodRecordMap = moodList.associateBy { it.id }
                    val exerciseByDate = exercises.groupBy { exercise ->
                        moodRecordMap[exercise.moodRecordId]?.date
                    }.filterKeys { it != null }
                        .mapKeys { it.key!! }
                        .mapValues { (_, exercisesForDate) ->
                            exercisesForDate.map { entity ->
                                ExerciseInfo(
                                    id = entity.id,
                                    type = ExerciseType.entries.firstOrNull {
                                        it.displayName == entity.type
                                    } ?: ExerciseType.OTHER,
                                    durationMinutes = entity.durationMinutes
                                )
                            }
                        }

                    _uiState.update { current ->
                        current.copy(
                            moods = moodList.associate { entity ->
                                entity.date to (KidMood.fromInt(entity.mood) ?: KidMood.OK)
                            },
                            exercises = exerciseByDate
                        )
                    }
                }
        }
    }

    fun setMonth(month: YearMonth) {
        _uiState.update { it.copy(month = month) }
        observeForCurrentMonth()
    }

    fun setMood(date: LocalDate, mood: KidMood) {
        val state = _uiState.value
        if (state.kidId == 0L) return

        viewModelScope.launch {
            val existingRecord = moodRepository.getMoodForKidOnDate(state.kidId, date)
            val entity = MoodRecordEntity(
                id = existingRecord?.id ?: 0L,
                kidId = state.kidId,
                date = date,
                mood = mood.value,
                note = existingRecord?.note,
                exerciseType = existingRecord?.exerciseType,
                exerciseDurationMinutes = existingRecord?.exerciseDurationMinutes
            )
            moodRepository.addOrUpdate(entity)
        }
    }

    fun clearMood(date: LocalDate) {
        val state = _uiState.value
        if (state.kidId == 0L) return

        viewModelScope.launch {
            moodRepository.deleteForKidOnDate(state.kidId, date)
        }
    }

    fun addExercise(date: LocalDate, type: ExerciseType, durationMinutes: Int) {
        val state = _uiState.value
        if (state.kidId == 0L) return

        viewModelScope.launch {
            // Get or create mood record for this date
            var moodRecord = moodRepository.getMoodForKidOnDate(state.kidId, date)
            if (moodRecord == null) {
                // Create a default mood record if none exists
                val newRecord = MoodRecordEntity(
                    kidId = state.kidId,
                    date = date,
                    mood = KidMood.OK.value,
                    note = null
                )
                val recordId = moodRepository.addOrUpdate(newRecord)
                moodRecord = newRecord.copy(id = recordId)
            }

            // Add exercise
            val exercise = ExerciseRecordEntity(
                moodRecordId = moodRecord.id,
                type = type.displayName,
                durationMinutes = durationMinutes
            )
            exerciseRepository.addExercise(exercise)
            refreshExerciseData()
        }
    }

    fun removeExercise(exerciseId: Long) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exerciseId)
            refreshExerciseData()
        }
    }

    fun updateExercise(exerciseId: Long, type: ExerciseType, durationMinutes: Int) {
        viewModelScope.launch {
            exerciseRepository.updateExercise(exerciseId, type.displayName, durationMinutes)
            refreshExerciseData()
        }
    }

    private suspend fun refreshExerciseData() {
        val state = _uiState.value
        if (state.kidId == 0L) return

        val month = state.month
        val start = month.atDay(1)
        val end = month.atEndOfMonth()

        val moodList = moodRepository.getMoodForKidInRange(state.kidId, start, end)
        val exercises = exerciseRepository.getExercisesForKidInRange(state.kidId, start, end)

        val moodRecordMap = moodList.associateBy { it.id }
        val exerciseByDate = exercises.groupBy { exercise ->
            moodRecordMap[exercise.moodRecordId]?.date
        }.filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { (_, exercisesForDate) ->
                exercisesForDate.map { entity ->
                    ExerciseInfo(
                        id = entity.id,
                        type = ExerciseType.entries.firstOrNull {
                            it.displayName == entity.type
                        } ?: ExerciseType.OTHER,
                        durationMinutes = entity.durationMinutes
                    )
                }
            }

        _uiState.update { current ->
            current.copy(
                moods = moodList.associate { entity ->
                    entity.date to (KidMood.fromInt(entity.mood) ?: KidMood.OK)
                },
                exercises = exerciseByDate
            )
        }
    }

    fun getTotalExerciseDuration(date: LocalDate): Int {
        return _uiState.value.exercises[date]?.sumOf { it.durationMinutes } ?: 0
    }
}

