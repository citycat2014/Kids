package com.example.kids.ui.kid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.repository.ExerciseRecordRepository
import com.example.kids.data.repository.KidRepository
import com.example.kids.data.repository.MoodRecordRepository
import com.example.kids.ui.mood.KidMood
import com.example.kids.ui.screens.KidListItemUi
import com.example.kids.ui.screens.TodaySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
            kidRepository.observeKids()
                .collect { list ->
                    val today = LocalDate.now()
                    val uiItems = list.map { entity ->
                        // Get today's mood and exercise
                        val todayMood = moodRepository.getMoodForKidOnDate(entity.id, today)
                        val todayExercises = exerciseRepository.getExercisesForKidOnDate(entity.id, today)

                        val mood = todayMood?.let { KidMood.fromInt(it.mood) }
                        val totalExerciseMinutes = todayExercises.sumOf { it.durationMinutes }

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
                            subtitle = "点击【成长记录】或【乖不乖日历】查看数据",
                            avatarUri = entity.avatarUri,
                            todaySummary = todaySummary
                        )
                    }
                    _kids.value = uiItems
                }
        }
    }

    // 目前添加和编辑都在详情页中完成，这里暂不提供快速添加
}

