package com.example.kids.data.repository

import com.example.kids.data.dao.KidDao
import com.example.kids.data.model.KidEntity
import kotlinx.coroutines.flow.Flow

class KidRepository(
    private val kidDao: KidDao
) {

    fun observeKids(): Flow<List<KidEntity>> = kidDao.observeKids()

    fun observeKid(id: Long): Flow<KidEntity?> = kidDao.observeKid(id)

    suspend fun addOrUpdateKid(kid: KidEntity): Long =
        if (kid.id == 0L) kidDao.insert(kid) else {
            kidDao.update(kid)
            kid.id
        }

    suspend fun deleteKid(kid: KidEntity) = kidDao.delete(kid)
}

