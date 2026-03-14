package com.example.kids.ui.kid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.repository.ExerciseRecordRepository
import com.example.kids.data.repository.KidRepository
import com.example.kids.data.repository.MoodRecordRepository
import com.example.kids.data.model.GrowthStandard
import com.example.kids.ui.mood.KidMood
import com.example.kids.ui.screens.KidListItemUi
import com.example.kids.ui.screens.TodaySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

class KidListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KidsDatabase.getInstance(application)
    private val kidRepository = KidRepository(database.kidDao())
    private val moodRepository = MoodRecordRepository(database.moodRecordDao())
    private val exerciseRepository = ExerciseRecordRepository(database.exerciseRecordDao())

    private val _kids = MutableStateFlow<List<KidListItemUi>>(emptyList())
    val kids: StateFlow<List<KidListItemUi>> = _kids.asStateFlow()

    init {
        viewModelScope.launch {
            val today = LocalDate.now()

            // Combine kids, moods, and exercises to refresh when any changes
            combine(
                kidRepository.observeKids(),
                moodRepository.observeAllMoodsForDate(today),
                exerciseRepository.observeAllExercisesForDate(today)
            ) { kids, moods, exercises ->
                val moodMap = moods.associateBy { it.kidId }
                val exerciseMap = exercises.groupBy { exercise ->
                    moods.find { it.id == exercise.moodRecordId }?.kidId
                }.filterKeys { it != null }.mapKeys { it.key!! }

                kids.map { entity ->
                    val mood = moodMap[entity.id]?.let { KidMood.fromInt(it.mood) }
                    val totalExerciseMinutes = exerciseMap[entity.id]?.sumOf { it.durationMinutes } ?: 0

                    val todaySummary = if (mood != null || totalExerciseMinutes > 0) {
                        TodaySummary(
                            mood = mood,
                            totalExerciseMinutes = totalExerciseMinutes
                        )
                    } else {
                        null
                    }

                    KidListItemUi(
                        id = entity.id,
                        name = entity.name.ifBlank { "未命名宝贝" },
                        subtitle = GrowthStandard.calculateAgeText(entity.birthday),
                        avatarUri = entity.avatarUri,
                        todaySummary = todaySummary
                    )
                }
            }.collect { uiItems ->
                _kids.value = uiItems
            }
        }
    }

    // 目前添加和编辑都在详情页中完成，这里暂不提供快速添加
}

