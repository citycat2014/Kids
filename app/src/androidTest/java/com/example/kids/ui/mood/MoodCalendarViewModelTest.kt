package com.example.kids.ui.mood

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.MoodRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class MoodCalendarViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KidsDatabase
    private lateinit var viewModel: MoodCalendarViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            KidsDatabase::class.java
        ).allowMainThreadQueries().build()

        // 使用反射替换数据库实例
        val field = KidsDatabase::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, database)

        viewModel = MoodCalendarViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_with_zero_kidId_does_not_update_state() = runTest {
        // When
        viewModel.load(0L)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(0L, state.kidId)
    }

    @Test
    fun load_updates_kidId_and_observes_moods() = runTest {
        // Given
        val kidId = 1L
        val record = MoodRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 1, 15),
            mood = KidMood.GOOD.value,
            note = "很乖",
            exerciseType = ExerciseType.RUNNING.displayName,
            exerciseDurationMinutes = 30
        )
        database.moodRecordDao().insert(record)

        // When
        viewModel.load(kidId)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(kidId, state.kidId)
        assertEquals(KidMood.GOOD, state.moods[LocalDate.of(2024, 1, 15)])
    }

    @Test
    fun setMonth_updates_state() = runTest {
        // Given
        val month = YearMonth.of(2024, 2)

        // When
        viewModel.setMonth(month)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(month, state.month)
    }

    @Test
    fun setMood_adds_mood_record() = runTest {
        // Given
        val kidId = 1L
        val date = LocalDate.of(2024, 1, 15)
        viewModel.load(kidId)

        // When
        viewModel.setMood(date, KidMood.GOOD)

        // Wait for the operation to complete
        Thread.sleep(100)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(KidMood.GOOD, state.moods[date])
    }

    @Test
    fun setMood_updates_existing_mood() = runTest {
        // Given
        val kidId = 1L
        val date = LocalDate.of(2024, 1, 15)
        val existingRecord = MoodRecordEntity(
            kidId = kidId,
            date = date,
            mood = KidMood.GOOD.value,
            note = "很乖",
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        database.moodRecordDao().insert(existingRecord)
        viewModel.load(kidId)

        // When
        viewModel.setMood(date, KidMood.BAD)

        // Wait for the operation to complete
        Thread.sleep(100)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(KidMood.BAD, state.moods[date])
    }

    @Test
    fun clearMood_removes_mood_record() = runTest {
        // Given
        val kidId = 1L
        val date = LocalDate.of(2024, 1, 15)
        val record = MoodRecordEntity(
            kidId = kidId,
            date = date,
            mood = KidMood.GOOD.value,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        database.moodRecordDao().insert(record)
        viewModel.load(kidId)

        // When
        viewModel.clearMood(date)

        // Wait for the operation to complete
        Thread.sleep(100)

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.moods[date] == null)
    }

    @Test
    fun setExercise_updates_exercise_info() = runTest {
        // Given
        val kidId = 1L
        val date = LocalDate.of(2024, 1, 15)
        viewModel.load(kidId)

        // When
        viewModel.setExercise(date, ExerciseType.RUNNING, 45)

        // Wait for the operation to complete
        Thread.sleep(100)

        // Then
        val state = viewModel.uiState.first()
        val exercise = state.exercises[date]
        assertEquals(ExerciseType.RUNNING, exercise?.type)
        assertEquals(45, exercise?.durationMinutes)
    }

    @Test
    fun load_observes_mood_for_current_month_only() = runTest {
        // Given
        val kidId = 1L
        val currentMonth = YearMonth.of(2024, 1)
        val recordInMonth = MoodRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 1, 15),
            mood = KidMood.GOOD.value,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        val recordOutMonth = MoodRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 2, 15),
            mood = KidMood.BAD.value,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        database.moodRecordDao().insert(recordInMonth)
        database.moodRecordDao().insert(recordOutMonth)

        // When
        viewModel.setMonth(currentMonth)
        viewModel.load(kidId)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(1, state.moods.size)
        assertEquals(KidMood.GOOD, state.moods[LocalDate.of(2024, 1, 15)])
    }
}