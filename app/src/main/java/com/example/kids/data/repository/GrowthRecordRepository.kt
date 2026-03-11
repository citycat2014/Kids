package com.example.kids.data.repository

import com.example.kids.data.dao.GrowthRecordDao
import com.example.kids.data.model.GrowthRecordEntity
import kotlinx.coroutines.flow.Flow

class GrowthRecordRepository(
    private val dao: GrowthRecordDao
) {

    fun observeRecordsForKid(kidId: Long): Flow<List<GrowthRecordEntity>> =
        dao.observeRecordsForKid(kidId)

    suspend fun addOrUpdate(record: GrowthRecordEntity): Long =
        if (record.id == 0L) {
            dao.insert(record)
        } else {
            dao.update(record)
            record.id
        }

    suspend fun delete(record: GrowthRecordEntity) =
        dao.delete(record)
}

