package com.example.kids.data.repository

import com.example.kids.data.dao.MoodRecordDao
import com.example.kids.data.model.MoodRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class MoodRecordRepositoryTest {

    @Mock
    private lateinit var mockDao: MoodRecordDao

    private lateinit var repository: MoodRecordRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = MoodRecordRepository(mockDao)
    }

    @Test
    fun observe_mood_for_kid_in_range_delegates_to_dao() = runTest {
        // Given
        val expectedRecords = listOf(
            MoodRecordEntity(1L, 1L, LocalDate.of(2024, 1, 1), 1, "很乖", null, null),
            MoodRecordEntity(2L, 1L, LocalDate.of(2024, 1, 2), 2, "一般", null, null)
        )
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        whenever(mockDao.observeMoodForKidInRange(1L, startDate, endDate))
            .thenReturn(flowOf(expectedRecords))

        // When
        val result = repository.observeMoodForKidInRange(1L, startDate, endDate).first()

        // Then
        assertEquals(expectedRecords, result)
    }

    @Test
    fun add_or_update_inserts_new_record() = runTest {
        // Given
        val newRecord = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 1,
            note = "很乖",
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        whenever(mockDao.insert(newRecord)).thenReturn(1L)

        // When
        val result = repository.addOrUpdate(newRecord)

        // Then
        assertEquals(1L, result)
        verify(mockDao).insert(newRecord)
    }

    @Test
    fun add_or_update_updates_existing_record() = runTest {
        // Given
        val existingRecord = MoodRecordEntity(
            id = 1L,
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 2,
            note = "更新",
            exerciseType = null,
            exerciseDurationMinutes = null
        )

        // When
        val result = repository.addOrUpdate(existingRecord)

        // Then
        assertEquals(1L, result)
        verify(mockDao).update(existingRecord)
    }

    @Test
    fun delete_for_kid_on_date_delegates_to_dao() = runTest {
        // Given
        val kidId = 1L
        val date = LocalDate.of(2024, 1, 1)

        // When
        repository.deleteForKidOnDate(kidId, date)

        // Then
        verify(mockDao).deleteForKidAndDate(kidId, date)
    }

    @Test
    fun get_mood_for_kid_on_date_delegates_to_dao() = runTest {
        // Given
        val expectedRecord = MoodRecordEntity(
            id = 1L,
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 1,
            note = "很乖",
            exerciseType = "RUNNING",
            exerciseDurationMinutes = 30
        )
        val date = LocalDate.of(2024, 1, 1)
        whenever(mockDao.getMoodForKidOnDate(1L, date)).thenReturn(expectedRecord)

        // When
        val result = repository.getMoodForKidOnDate(1L, date)

        // Then
        assertEquals(expectedRecord, result)
    }
}