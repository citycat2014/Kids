package com.example.kids.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kids.data.model.ExerciseRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseRecordDao {

    @Query(
        """
        SELECT er.* FROM exercise_records er
        INNER JOIN mood_records mr ON er.moodRecordId = mr.id
        WHERE mr.kidId = :kidId
        AND mr.date BETWEEN :startDate AND :endDate
        """
    )
    fun observeExercisesForKidInRange(
        kidId: Long,
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate
    ): Flow<List<ExerciseRecordEntity>>

    @Query(
        """
        SELECT er.* FROM exercise_records er
        INNER JOIN mood_records mr ON er.moodRecordId = mr.id
        WHERE mr.kidId = :kidId
        AND mr.date = :date
        """
    )
    suspend fun getExercisesForKidOnDate(
        kidId: Long,
        date: java.time.LocalDate
    ): List<ExerciseRecordEntity>

    @Query(
        """
        SELECT er.* FROM exercise_records er
        INNER JOIN mood_records mr ON er.moodRecordId = mr.id
        WHERE mr.kidId = :kidId
        AND mr.date BETWEEN :startDate AND :endDate
        """
    )
    suspend fun getExercisesForKidInRange(
        kidId: Long,
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate
    ): List<ExerciseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseRecordEntity): Long

    @Query("DELETE FROM exercise_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE exercise_records SET type = :type, durationMinutes = :durationMinutes WHERE id = :id")
    suspend fun update(id: Long, type: String, durationMinutes: Int)

    @Query(
        """
        DELETE FROM exercise_records
        WHERE moodRecordId IN (
            SELECT id FROM mood_records WHERE kidId = :kidId AND date = :date
        )
        """
    )
    suspend fun deleteExercisesForKidOnDate(kidId: Long, date: java.time.LocalDate)
}