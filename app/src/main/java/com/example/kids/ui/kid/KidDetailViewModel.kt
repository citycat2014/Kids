package com.example.kids.ui.kid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.KidEntity
import com.example.kids.data.repository.KidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.LocalDate

data class KidDetailUiState(
    val id: Long = 0L,
    val name: String = "",
    val gender: String = "",
    val birthday: LocalDate? = null,
    val avatarUri: String? = null,
    val isNew: Boolean = true
)

class KidDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = KidRepository(KidsDatabase.getInstance(application).kidDao())

    private val _uiState = MutableStateFlow(KidDetailUiState())
    val uiState: StateFlow<KidDetailUiState> = _uiState.asStateFlow()

    fun load(kidId: Long?) {
        if (kidId == null || kidId == 0L) {
            _uiState.value = KidDetailUiState()
            return
        }
        viewModelScope.launch {
            repository.observeKid(kidId)
                .filterNotNull()
                .collect { entity ->
                    _uiState.value = KidDetailUiState(
                        id = entity.id,
                        name = entity.name,
                        gender = entity.gender,
                        birthday = entity.birthday,
                        avatarUri = entity.avatarUri,
                        isNew = false
                    )
                }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateBirthday(birthday: LocalDate?) {
        _uiState.value = _uiState.value.copy(birthday = birthday)
    }

    fun updateAvatar(uri: String?) {
        _uiState.value = _uiState.value.copy(avatarUri = uri)
    }

    suspend fun save(): Long {
        val state = _uiState.value
        val entity = KidEntity(
            id = state.id,
            name = state.name,
            gender = state.gender,
            birthday = state.birthday,
            avatarUri = state.avatarUri
        )
        return repository.addOrUpdateKid(entity)
    }
}

