package com.example.kids.data.repository

import com.example.kids.data.dao.GrowthRecordDao
import com.example.kids.data.model.GrowthRecordEntity
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

class GrowthRecordRepositoryTest {

    @Mock
    private lateinit var mockDao: GrowthRecordDao

    private lateinit var repository: GrowthRecordRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = GrowthRecordRepository(mockDao)
    }

    @Test
    fun observe_records_for_kid_delegates_to_dao() = runTest {
        // Given
        val expectedRecords = listOf(
            GrowthRecordEntity(1L, 1L, LocalDate.of(2024, 1, 1), 100.0f, 18.0f, null, null),
            GrowthRecordEntity(2L, 1L, LocalDate.of(2024, 2, 1), 105.0f, 19.0f, null, null)
        )
        whenever(mockDao.observeRecordsForKid(1L)).thenReturn(flowOf(expectedRecords))

        // When
        val result = repository.observeRecordsForKid(1L).first()

        // Then
        assertEquals(expectedRecords, result)
    }

    @Test
    fun add_or_update_inserts_new_record() = runTest {
        // Given
        val newRecord = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
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
        val existingRecord = GrowthRecordEntity(
            id = 1L,
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 105.0f,
            weightKg = 19.0f,
            note = "更新",
            photoUri = null
        )

        // When
        val result = repository.addOrUpdate(existingRecord)

        // Then
        assertEquals(1L, result)
        verify(mockDao).update(existingRecord)
    }

    @Test
    fun delete_delegates_to_dao() = runTest {
        // Given
        val record = GrowthRecordEntity(
            id = 1L,
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )

        // When
        repository.delete(record)

        // Then
        verify(mockDao).delete(record)
    }
}