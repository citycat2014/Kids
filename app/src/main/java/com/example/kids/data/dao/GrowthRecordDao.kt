package com.example.kids.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kids.data.model.GrowthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthRecordDao {

    @Query(
        """
        SELECT * FROM growth_records
        WHERE kidId = :kidId
        ORDER BY date DESC
        """
    )
    fun observeRecordsForKid(kidId: Long): Flow<List<GrowthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: GrowthRecordEntity): Long

    @Update
    suspend fun update(record: GrowthRecordEntity)

    @Delete
    suspend fun delete(record: GrowthRecordEntity)
}

