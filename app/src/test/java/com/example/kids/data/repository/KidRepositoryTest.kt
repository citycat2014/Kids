package com.example.kids.data.repository

import com.example.kids.data.dao.KidDao
import com.example.kids.data.model.KidEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class KidRepositoryTest {

    @Mock
    private lateinit var mockKidDao: KidDao

    private lateinit var kidRepository: KidRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        kidRepository = KidRepository(mockKidDao)
    }

    @Test
    fun observe_kids_delegates_to_dao() = runTest {
        // Given
        val expectedKids = listOf(
            KidEntity(1L, "小明", "男", null, null),
            KidEntity(2L, "小红", "女", null, null)
        )
        whenever(mockKidDao.observeKids()).thenReturn(flowOf(expectedKids))

        // When
        val result = kidRepository.observeKids().first()

        // Then
        assertEquals(expectedKids, result)
    }

    @Test
    fun observe_kid_delegates_to_dao() = runTest {
        // Given
        val expectedKid = KidEntity(1L, "小明", "男", LocalDate.of(2020, 1, 1), null)
        whenever(mockKidDao.observeKid(1L)).thenReturn(flowOf(expectedKid))

        // When
        val result = kidRepository.observeKid(1L).first()

        // Then
        assertEquals(expectedKid, result)
    }

    @Test
    fun add_or_update_inserts_new_kid() = runTest {
        // Given
        val newKid = KidEntity(0L, "小明", "男", null, null)
        whenever(mockKidDao.insert(newKid)).thenReturn(1L)

        // When
        val result = kidRepository.addOrUpdateKid(newKid)

        // Then
        assertEquals(1L, result)
        verify(mockKidDao).insert(newKid)
    }

    @Test
    fun add_or_update_updates_existing_kid() = runTest {
        // Given
        val existingKid = KidEntity(1L, "小明", "男", null, null)

        // When
        val result = kidRepository.addOrUpdateKid(existingKid)

        // Then
        assertEquals(1L, result)
        verify(mockKidDao).update(existingKid)
    }

    @Test
    fun delete_kid_delegates_to_dao() = runTest {
        // Given
        val kid = KidEntity(1L, "小明", "男", null, null)

        // When
        kidRepository.deleteKid(kid)

        // Then
        verify(mockKidDao).delete(kid)
    }
}