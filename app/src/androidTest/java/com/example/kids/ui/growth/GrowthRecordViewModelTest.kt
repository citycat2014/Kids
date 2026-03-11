package com.example.kids.ui.growth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.GrowthRecordEntity
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

@RunWith(AndroidJUnit4::class)
class GrowthRecordViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KidsDatabase
    private lateinit var viewModel: GrowthRecordViewModel

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

        viewModel = GrowthRecordViewModel(ApplicationProvider.getApplicationContext())
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
        assertTrue(state.records.isEmpty())
    }

    @Test
    fun load_updates_kidId_and_observes_records() = runTest {
        // Given
        val kidId = 1L
        val record = GrowthRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = "健康",
            photoUri = null
        )
        database.growthRecordDao().insert(record)

        // When
        viewModel.load(kidId)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(kidId, state.kidId)
        assertEquals(1, state.records.size)
        assertEquals(100.0f, state.records[0].heightCm)
    }

    @Test
    fun records_are_sorted_by_date() = runTest {
        // Given
        val kidId = 1L
        val record1 = GrowthRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 3, 1),
            heightCm = 105.0f,
            weightKg = 19.0f,
            note = null,
            photoUri = null
        )
        val record2 = GrowthRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )
        database.growthRecordDao().insert(record1)
        database.growthRecordDao().insert(record2)

        // When
        viewModel.load(kidId)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(2, state.records.size)
        assertTrue(state.records[0].date < state.records[1].date)
    }

    @Test
    fun addOrUpdate_inserts_new_record() = runTest {
        // Given
        val kidId = 1L
        viewModel.load(kidId)

        // When
        viewModel.addOrUpdate(
            id = 0L,
            kidId = kidId,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = "新记录",
            photoUri = null
        )

        // Wait for the coroutine to complete
        val state = viewModel.uiState.first()
        while (state.records.isEmpty()) {
            Thread.sleep(100)
        }

        // Then
        val finalState = viewModel.uiState.first()
        assertEquals(1, finalState.records.size)
        assertEquals(100.0f, finalState.records[0].heightCm)
    }

    @Test
    fun delete_removes_record() = runTest {
        // Given
        val kidId = 1L
        val record = GrowthRecordEntity(
            kidId = kidId,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )
        val recordId = database.growthRecordDao().insert(record)
        viewModel.load(kidId)

        // Wait for records to load
        var state = viewModel.uiState.first()
        while (state.records.isEmpty()) {
            Thread.sleep(100)
            state = viewModel.uiState.first()
        }

        // When
        viewModel.delete(recordId)

        // Wait for deletion
        Thread.sleep(100)

        // Then
        val finalState = viewModel.uiState.first()
        assertTrue(finalState.records.isEmpty())
    }
}