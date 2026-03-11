package com.example.kids.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kids.data.model.MoodRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodRecordDao {

    @Query(
        """
        SELECT * FROM mood_records
        WHERE kidId = :kidId
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
        """
    )
    fun observeMoodForKidInRange(
        kidId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<MoodRecordEntity>>

    @Query(
        """
        SELECT * FROM mood_records
        WHERE kidId = :kidId
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
        """
    )
    suspend fun getMoodForKidInRange(
        kidId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MoodRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MoodRecordEntity): Long

    @Update
    suspend fun update(record: MoodRecordEntity)

    @Query("DELETE FROM mood_records WHERE kidId = :kidId AND date = :date")
    suspend fun deleteForKidAndDate(kidId: Long, date: LocalDate)

    @Query("SELECT * FROM mood_records WHERE kidId = :kidId AND date = :date LIMIT 1")
    suspend fun getMoodForKidOnDate(kidId: Long, date: LocalDate): MoodRecordEntity?

    @Query("SELECT * FROM mood_records WHERE date = :date")
    fun observeAllMoodsForDate(date: LocalDate): Flow<List<MoodRecordEntity>>
}

