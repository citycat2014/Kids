package com.example.kids.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "growth_records")
data class GrowthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val kidId: Long,
    val date: LocalDate,
    val heightCm: Float?,
    val weightKg: Float?,
    val note: String?,
    val photoUri: String?,
    val latitude: Double? = null,
    val longitude: Double? = null
)

