package com.example.kids.ui.growth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.GrowthRecordEntity
import com.example.kids.data.model.GrowthStandard
import com.example.kids.data.model.KidEntity
import com.example.kids.data.repository.GrowthRecordRepository
import com.example.kids.data.repository.KidRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val longitude: Double?,
    val analysis: GrowthStandard.AnalysisResult? = null
)

data class GrowthRecordUiState(
    val kidId: Long = 0L,
    val kidName: String = "",
    val kidGender: String = "",
    val kidBirthday: LocalDate? = null,
    val records: List<GrowthRecordUi> = emptyList()
)

class GrowthRecordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = GrowthRecordRepository(
        KidsDatabase.getInstance(application).growthRecordDao()
    )

    private val kidRepository = KidRepository(
        KidsDatabase.getInstance(application).kidDao()
    )

    private val _uiState = MutableStateFlow(GrowthRecordUiState())
    val uiState: StateFlow<GrowthRecordUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun load(kidId: Long) {
        if (kidId == 0L || kidId == _uiState.value.kidId) return

        // 取消之前的 Flow 收集
        observeJob?.cancel()
        _uiState.value = GrowthRecordUiState(kidId = kidId)

        observeJob = viewModelScope.launch {
            // 同时观察 Kid 信息和成长记录
            combine(
                kidRepository.observeKid(kidId),
                repository.observeRecordsForKid(kidId)
            ) { kid, records ->
                Pair(kid, records)
            }.collect { (kid, records) ->
                _uiState.update { state ->
                    state.copy(
                        kidName = kid?.name ?: "",
                        kidGender = kid?.gender ?: "",
                        kidBirthday = kid?.birthday,
                        records = records.map { entity ->
                            val ageInYears = GrowthStandard.calculateAgeInYears(
                                kid?.birthday,
                                entity.date
                            )
                            val analysis = if (ageInYears != null &&
                                (kid?.gender == "男" || kid?.gender == "女")
                            ) {
                                GrowthStandard.analyze(
                                    gender = kid.gender,
                                    ageInYears = ageInYears,
                                    heightCm = entity.heightCm,
                                    weightKg = entity.weightKg
                                )
                            } else null

                            GrowthRecordUi(
                                id = entity.id,
                                date = entity.date,
                                heightCm = entity.heightCm,
                                weightKg = entity.weightKg,
                                note = entity.note,
                                photoUri = entity.photoUri,
                                latitude = entity.latitude,
                                longitude = entity.longitude,
                                analysis = analysis
                            )
                        }.sortedBy { it.date }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
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

