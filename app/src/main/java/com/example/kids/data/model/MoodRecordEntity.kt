package com.example.kids.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "mood_records")
data class MoodRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val kidId: Long,
    val date: LocalDate,
    val mood: Int,
    val note: String?,
    val exerciseType: String? = null,
    val exerciseDurationMinutes: Int? = null
)

