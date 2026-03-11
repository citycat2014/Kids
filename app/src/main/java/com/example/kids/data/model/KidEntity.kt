package com.example.kids.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "kids")
data class KidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val gender: String,
    val birthday: LocalDate?,
    val avatarUri: String?
)

