package com.example.kids.data.repository

import com.example.kids.data.dao.MoodRecordDao
import com.example.kids.data.model.MoodRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MoodRecordRepository(
    private val dao: MoodRecordDao
) {

    fun observeMoodForKidInRange(
        kidId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<MoodRecordEntity>> =
        dao.observeMoodForKidInRange(kidId, startDate, endDate)

    suspend fun getMoodForKidInRange(
        kidId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MoodRecordEntity> =
        dao.getMoodForKidInRange(kidId, startDate, endDate)

    suspend fun addOrUpdate(record: MoodRecordEntity): Long =
        if (record.id == 0L) {
            dao.insert(record)
        } else {
            dao.update(record)
            record.id
        }

    suspend fun deleteForKidOnDate(kidId: Long, date: LocalDate) =
        dao.deleteForKidAndDate(kidId, date)

    suspend fun getMoodForKidOnDate(kidId: Long, date: LocalDate): MoodRecordEntity? =
        dao.getMoodForKidOnDate(kidId, date)
}

