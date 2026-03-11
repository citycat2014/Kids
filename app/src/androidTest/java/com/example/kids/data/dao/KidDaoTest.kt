package com.example.kids.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.KidEntity
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
class KidDaoTest {

    private lateinit var database: KidsDatabase
    private lateinit var kidDao: KidDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            KidsDatabase::class.java
        ).allowMainThreadQueries().build()
        kidDao = database.kidDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insert_and_observe_kid() = runTest {
        // Given
        val kid = KidEntity(
            name = "小明",
            gender = "男",
            birthday = LocalDate.of(2020, 1, 1),
            avatarUri = null
        )

        // When
        val id = kidDao.insert(kid)
        val observedKid = kidDao.observeKid(id).first()

        // Then
        assertEquals(id, observedKid?.id)
        assertEquals("小明", observedKid?.name)
        assertEquals("男", observedKid?.gender)
    }

    @Test
    fun observe_all_kids() = runTest {
        // Given
        val kid1 = KidEntity(name = "小明", gender = "男", birthday = null, avatarUri = null)
        val kid2 = KidEntity(name = "小红", gender = "女", birthday = null, avatarUri = null)

        // When
        kidDao.insert(kid1)
        kidDao.insert(kid2)
        val allKids = kidDao.observeKids().first()

        // Then
        assertEquals(2, allKids.size)
        assertEquals("小明", allKids[0].name)
        assertEquals("小红", allKids[1].name)
    }

    @Test
    fun update_kid() = runTest {
        // Given
        val kid = KidEntity(
            name = "小明",
            gender = "男",
            birthday = null,
            avatarUri = null
        )
        val id = kidDao.insert(kid)

        // When
        val updatedKid = kid.copy(id = id, name = "小明明")
        kidDao.update(updatedKid)
        val observedKid = kidDao.observeKid(id).first()

        // Then
        assertEquals("小明明", observedKid?.name)
    }

    @Test
    fun delete_kid() = runTest {
        // Given
        val kid = KidEntity(
            name = "小明",
            gender = "男",
            birthday = null,
            avatarUri = null
        )
        val id = kidDao.insert(kid)

        // When
        val insertedKid = kidDao.observeKid(id).first()
        kidDao.delete(insertedKid!!)
        val deletedKid = kidDao.observeKid(id).first()

        // Then
        assertNull(deletedKid)
    }
}