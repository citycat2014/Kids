package com.example.kids.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kids.data.model.KidEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KidDao {

    @Query("SELECT * FROM kids ORDER BY id ASC")
    fun observeKids(): Flow<List<KidEntity>>

    @Query("SELECT * FROM kids WHERE id = :id LIMIT 1")
    fun observeKid(id: Long): Flow<KidEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kid: KidEntity): Long

    @Update
    suspend fun update(kid: KidEntity)

    @Delete
    suspend fun delete(kid: KidEntity)
}

