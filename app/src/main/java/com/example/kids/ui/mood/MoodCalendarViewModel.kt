package com.example.kids.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.MoodRecordEntity
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
    val type: ExerciseType?,
    val durationMinutes: Int?
)

data class MoodCalendarUiState(
    val kidId: Long = 0L,
    val month: YearMonth = YearMonth.now(),
    val moods: Map<LocalDate, KidMood> = emptyMap(),
    val exercises: Map<LocalDate, ExerciseInfo> = emptyMap()
)

class MoodCalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = MoodRecordRepository(
        KidsDatabase.getInstance(application).moodRecordDao()
    )

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
            repository.observeMoodForKidInRange(state.kidId, start, end)
                .collect { list ->
                    _uiState.update { current ->
                        current.copy(
                            moods = list.associate { entity ->
                                entity.date to (KidMood.fromInt(entity.mood) ?: KidMood.OK)
                            },
                            exercises = list.associate { entity ->
                                entity.date to ExerciseInfo(
                                    type = entity.exerciseType?.let { typeStr ->
                                        ExerciseType.entries.firstOrNull { it.displayName == typeStr }
                                    },
                                    durationMinutes = entity.exerciseDurationMinutes
                                )
                            }
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
            val existingRecord = repository.getMoodForKidOnDate(state.kidId, date)
            val entity = MoodRecordEntity(
                id = existingRecord?.id ?: 0L,
                kidId = state.kidId,
                date = date,
                mood = mood.value,
                note = existingRecord?.note,
                exerciseType = existingRecord?.exerciseType,
                exerciseDurationMinutes = existingRecord?.exerciseDurationMinutes
            )
            repository.addOrUpdate(entity)
        }
    }

    fun clearMood(date: LocalDate) {
        val state = _uiState.value
        if (state.kidId == 0L) return

        viewModelScope.launch {
            repository.deleteForKidOnDate(state.kidId, date)
        }
    }

    fun setExercise(date: LocalDate, type: ExerciseType?, durationMinutes: Int?) {
        val state = _uiState.value
        if (state.kidId == 0L) return

        viewModelScope.launch {
            val existingRecord = repository.getMoodForKidOnDate(state.kidId, date)
            val entity = MoodRecordEntity(
                id = existingRecord?.id ?: 0L,
                kidId = state.kidId,
                date = date,
                mood = existingRecord?.mood ?: KidMood.OK.value,
                note = existingRecord?.note,
                exerciseType = type?.displayName,
                exerciseDurationMinutes = durationMinutes
            )
            repository.addOrUpdate(entity)
        }
    }
}

