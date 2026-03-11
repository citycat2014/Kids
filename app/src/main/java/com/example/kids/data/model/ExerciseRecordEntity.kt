package com.example.kids.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_records",
    foreignKeys = [
        ForeignKey(
            entity = MoodRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["moodRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val moodRecordId: Long,
    val type: String,
    val durationMinutes: Int
)