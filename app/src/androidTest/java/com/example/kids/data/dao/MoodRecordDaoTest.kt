package com.example.kids.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.MoodRecordEntity
import com.example.kids.ui.mood.ExerciseType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MoodRecordDaoTest {

    private lateinit var database: KidsDatabase
    private lateinit var moodRecordDao: MoodRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            KidsDatabase::class.java
        ).allowMainThreadQueries().build()
        moodRecordDao = database.moodRecordDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insert_and_observe_mood_record() = runTest {
        // Given
        val record = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 1,
            note = "今天很乖",
            exerciseType = ExerciseType.RUNNING.name,
            exerciseDurationMinutes = 30
        )

        // When
        moodRecordDao.insert(record)
        val observedRecords = moodRecordDao.observeMoodForKidInRange(
            1L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        ).first()

        // Then
        assertEquals(1, observedRecords.size)
        assertEquals(1, observedRecords[0].mood)
        assertEquals("今天很乖", observedRecords[0].note)
    }

    @Test
    fun observe_mood_in_date_range() = runTest {
        // Given
        val record1 = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 5),
            mood = 1,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        val record2 = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 15),
            mood = 2,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        val record3 = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 25),
            mood = 3,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )

        // When
        moodRecordDao.insert(record1)
        moodRecordDao.insert(record2)
        moodRecordDao.insert(record3)

        val recordsInRange = moodRecordDao.observeMoodForKidInRange(
            1L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 20)
        ).first()

        // Then
        assertEquals(2, recordsInRange.size)
    }

    @Test
    fun update_mood_record() = runTest {
        // Given
        val record = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 1,
            note = "今天很乖",
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        val id = moodRecordDao.insert(record)

        // When
        val updatedRecord = record.copy(id = id, mood = 2, note = "今天一般")
        moodRecordDao.update(updatedRecord)
        val observedRecords = moodRecordDao.observeMoodForKidInRange(
            1L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        ).first()

        // Then
        assertEquals(2, observedRecords[0].mood)
        assertEquals("今天一般", observedRecords[0].note)
    }

    @Test
    fun delete_mood_for_kid_and_date() = runTest {
        // Given
        val record = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 1,
            note = null,
            exerciseType = null,
            exerciseDurationMinutes = null
        )
        moodRecordDao.insert(record)

        // When
        moodRecordDao.deleteForKidAndDate(1L, LocalDate.of(2024, 1, 1))
        val observedRecords = moodRecordDao.observeMoodForKidInRange(
            1L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31)
        ).first()

        // Then
        assertEquals(0, observedRecords.size)
    }

    @Test
    fun get_mood_for_kid_on_date() = runTest {
        // Given
        val record = MoodRecordEntity(
            kidId = 1L,
            date = LocalDate.of(2024, 1, 1),
            mood = 1,
            note = "今天很乖",
            exerciseType = ExerciseType.RUNNING.name,
            exerciseDurationMinutes = 45
        )
        moodRecordDao.insert(record)

        // When
        val retrievedRecord = moodRecordDao.getMoodForKidOnDate(1L, LocalDate.of(2024, 1, 1))
        val nonExistentRecord = moodRecordDao.getMoodForKidOnDate(1L, LocalDate.of(2024, 1, 2))

        // Then
        assertEquals(1, retrievedRecord?.mood)
        assertEquals("SWIMMING", retrievedRecord?.exerciseType)
        assertNull(nonExistentRecord)
    }
}