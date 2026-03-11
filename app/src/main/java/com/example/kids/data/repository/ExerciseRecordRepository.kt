package com.example.kids.data.repository

import com.example.kids.data.dao.ExerciseRecordDao
import com.example.kids.data.model.ExerciseRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ExerciseRecordRepository(
    private val dao: ExerciseRecordDao
) {

    fun observeExercisesForKidInRange(
        kidId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<ExerciseRecordEntity>> =
        dao.observeExercisesForKidInRange(kidId, startDate, endDate)

    suspend fun getExercisesForKidOnDate(
        kidId: Long,
        date: LocalDate
    ): List<ExerciseRecordEntity> =
        dao.getExercisesForKidOnDate(kidId, date)

    suspend fun getExercisesForKidInRange(
        kidId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ExerciseRecordEntity> =
        dao.getExercisesForKidInRange(kidId, startDate, endDate)

    suspend fun addExercise(exercise: ExerciseRecordEntity): Long =
        dao.insert(exercise)

    suspend fun updateExercise(id: Long, type: String, durationMinutes: Int) =
        dao.update(id, type, durationMinutes)

    suspend fun deleteExercise(id: Long) =
        dao.deleteById(id)

    suspend fun deleteExercisesForKidOnDate(kidId: Long, date: LocalDate) =
        dao.deleteExercisesForKidOnDate(kidId, date)

    fun observeAllExercisesForDate(date: LocalDate): Flow<List<ExerciseRecordEntity>> =
        dao.observeAllExercisesForDate(date)
}