package com.example.kids.ui.kid

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.KidEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class KidDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KidsDatabase
    private lateinit var viewModel: KidDetailViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            KidsDatabase::class.java
        ).allowMainThreadQueries().build()

        // 使用反射替换数据库实例
        val field = KidsDatabase::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, database)

        viewModel = KidDetailViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_with_null_kidId_shows_new_state() = runTest {
        // When
        viewModel.load(null)

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isNew)
        assertEquals(0L, state.id)
    }

    @Test
    fun load_with_zero_kidId_shows_new_state() = runTest {
        // When
        viewModel.load(0L)

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isNew)
    }

    @Test
    fun load_existing_kid_updates_state() = runTest {
        // Given
        val kid = KidEntity(
            name = "小明",
            gender = "男",
            birthday = LocalDate.of(2020, 1, 1),
            avatarUri = null
        )
        val id = database.kidDao().insert(kid)

        // When
        viewModel.load(id)

        // Then
        val state = viewModel.uiState.first()
        assertEquals("小明", state.name)
        assertEquals("男", state.gender)
        assertFalse(state.isNew)
    }

    @Test
    fun update_name_updates_state() = runTest {
        // When
        viewModel.updateName("小红")

        // Then
        val state = viewModel.uiState.first()
        assertEquals("小红", state.name)
    }

    @Test
    fun update_gender_updates_state() = runTest {
        // When
        viewModel.updateGender("女")

        // Then
        val state = viewModel.uiState.first()
        assertEquals("女", state.gender)
    }

    @Test
    fun update_birthday_updates_state() = runTest {
        // Given
        val birthday = LocalDate.of(2021, 5, 15)

        // When
        viewModel.updateBirthday(birthday)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(birthday, state.birthday)
    }

    @Test
    fun update_avatar_updates_state() = runTest {
        // Given
        val uri = "content://avatar/1"

        // When
        viewModel.updateAvatar(uri)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(uri, state.avatarUri)
    }

    @Test
    fun save_new_kid_inserts_to_database() = runTest {
        // Given
        viewModel.updateName("小华")
        viewModel.updateGender("男")
        viewModel.updateBirthday(LocalDate.of(2022, 1, 1))

        // When
        val id = viewModel.save()

        // Then
        assertTrue(id > 0)
        val savedKid = database.kidDao().observeKid(id).first()
        assertEquals("小华", savedKid?.name)
        assertEquals("男", savedKid?.gender)
    }

    @Test
    fun save_existing_kid_updates_database() = runTest {
        // Given
        val kid = KidEntity(
            name = "小明",
            gender = "男",
            birthday = null,
            avatarUri = null
        )
        val id = database.kidDao().insert(kid)
        viewModel.load(id)
        viewModel.updateName("小明明")

        // When
        val savedId = viewModel.save()

        // Then
        assertEquals(id, savedId)
        val updatedKid = database.kidDao().observeKid(id).first()
        assertEquals("小明明", updatedKid?.name)
    }
}