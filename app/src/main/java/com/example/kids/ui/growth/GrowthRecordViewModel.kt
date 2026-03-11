package com.example.kids.ui.growth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.GrowthRecordEntity
import com.example.kids.data.repository.GrowthRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GrowthRecordUi(
    val id: Long,
    val date: LocalDate,
    val heightCm: Float?,
    val weightKg: Float?,
    val note: String?,
    val photoUri: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class GrowthRecordUiState(
    val kidId: Long = 0L,
    val records: List<GrowthRecordUi> = emptyList()
)

class GrowthRecordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = GrowthRecordRepository(
        KidsDatabase.getInstance(application).growthRecordDao()
    )

    private val _uiState = MutableStateFlow(GrowthRecordUiState())
    val uiState: StateFlow<GrowthRecordUiState> = _uiState.asStateFlow()

    fun load(kidId: Long) {
        if (kidId == 0L || kidId == _uiState.value.kidId) return

        _uiState.value = GrowthRecordUiState(kidId = kidId)

        viewModelScope.launch {
            repository.observeRecordsForKid(kidId)
                .collect { list ->
                    _uiState.update { state ->
                        state.copy(
                            records = list.map {
                                GrowthRecordUi(
                                    id = it.id,
                                    date = it.date,
                                    heightCm = it.heightCm,
                                    weightKg = it.weightKg,
                                    note = it.note,
                                    photoUri = it.photoUri,
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            }.sortedBy { it.date }
                        )
                    }
                }
        }
    }

    fun addOrUpdate(
        id: Long,
        kidId: Long,
        date: LocalDate,
        heightCm: Float?,
        weightKg: Float?,
        note: String?,
        photoUri: String?,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val entity = GrowthRecordEntity(
                id = id,
                kidId = kidId,
                date = date,
                heightCm = heightCm,
                weightKg = weightKg,
                note = note,
                photoUri = photoUri,
                latitude = latitude,
                longitude = longitude
            )
            repository.addOrUpdate(entity)
        }
    }

    fun delete(recordId: Long) {
        val state = _uiState.value
        val target = state.records.firstOrNull { it.id == recordId } ?: return
        viewModelScope.launch {
            val entity = GrowthRecordEntity(
                id = target.id,
                kidId = state.kidId,
                date = target.date,
                heightCm = target.heightCm,
                weightKg = target.weightKg,
                note = target.note,
                photoUri = target.photoUri,
                latitude = target.latitude,
                longitude = target.longitude
            )
            repository.delete(entity)
        }
    }
}

