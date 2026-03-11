package com.example.kids.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.GrowthRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class GrowthRecordDaoTest {

    private lateinit var database: KidsDatabase
    private lateinit var growthRecordDao: GrowthRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            KidsDatabase::class.java
        ).allowMainThreadQueries().build()
        growthRecordDao = database.growthRecordDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insert_and_observe_growth_record() = runTest {
        // Given
        val record = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.5f,
            weightKg = 18.5f,
            note = "健康成长",
            photoUri = null
        )

        // When
        val id = growthRecordDao.insert(record)
        val observedRecords = growthRecordDao.observeRecordsForKid(1L).first()

        // Then
        assertEquals(1, observedRecords.size)
        assertEquals(100.5f, observedRecords[0].heightCm)
        assertEquals(18.5f, observedRecords[0].weightKg)
    }

    @Test
    fun observe_records_for_specific_kid() = runTest {
        // Given
        val record1 = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )
        val record2 = GrowthRecordEntity(
            kidId = 2L,
            date = LocalDate.of(2024, 1, 2),
            heightCm = 110.0f,
            weightKg = 20.0f,
            note = null,
            photoUri = null
        )

        // When
        growthRecordDao.insert(record1)
        growthRecordDao.insert(record2)
        val kid1Records = growthRecordDao.observeRecordsForKid(1L).first()
        val kid2Records = growthRecordDao.observeRecordsForKid(2L).first()

        // Then
        assertEquals(1, kid1Records.size)
        assertEquals(100.0f, kid1Records[0].heightCm)
        assertEquals(1, kid2Records.size)
        assertEquals(110.0f, kid2Records[0].heightCm)
    }

    @Test
    fun update_growth_record() = runTest {
        // Given
        val record = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )
        val id = growthRecordDao.insert(record)

        // When
        val updatedRecord = record.copy(id = id, heightCm = 105.0f)
        growthRecordDao.update(updatedRecord)
        val observedRecords = growthRecordDao.observeRecordsForKid(1L).first()

        // Then
        assertEquals(105.0f, observedRecords[0].heightCm)
    }

    @Test
    fun delete_growth_record() = runTest {
        // Given
        val record = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )
        val id = growthRecordDao.insert(record)

        // When
        val insertedRecord = growthRecordDao.observeRecordsForKid(1L).first().first()
        growthRecordDao.delete(insertedRecord)
        val deletedRecords = growthRecordDao.observeRecordsForKid(1L).first()

        // Then
        assertEquals(0, deletedRecords.size)
    }

    @Test
    fun records_ordered_by_date_desc() = runTest {
        // Given
        val record1 = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            heightCm = 100.0f,
            weightKg = 18.0f,
            note = null,
            photoUri = null
        )
        val record2 = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 3),
            heightCm = 105.0f,
            weightKg = 19.0f,
            note = null,
            photoUri = null
        )
        val record3 = GrowthRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 2),
            heightCm = 102.0f,
            weightKg = 18.5f,
            note = null,
            photoUri = null
        )

        // When
        growthRecordDao.insert(record1)
        growthRecordDao.insert(record2)
        growthRecordDao.insert(record3)
        val records = growthRecordDao.observeRecordsForKid(1L).first()

        // Then
        assertEquals(3, records.size)
        assertEquals(LocalDate.of(2024, 1, 3), records[0].date)
        assertEquals(LocalDate.of(2024, 1, 2), records[1].date)
        assertEquals(LocalDate.of(2024, 1, 1), records[2].date)
    }
}