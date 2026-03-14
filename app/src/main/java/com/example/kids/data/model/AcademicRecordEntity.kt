package com.example.kids.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "academic_records",
    indices = [
        Index(value = ["kidId"]),
        Index(value = ["examDate"])
    ]
)
data class AcademicRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val kidId: Long,
    val examName: String,           // 考试名称，如"期中考试"
    val examDate: LocalDate,        // 考试日期
    val examType: String,           // 考试类型：平时/期中/期末/模拟
    val gradeLevel: String,         // 年级，如"小学三年级"
    val semester: String,           // 学期：上学期/下学期
    val subject: String,            // 科目名称
    val score: Float?,              // 主分数（非附加分部分）
    val bonusScore: Float?,         // 附加分（可选）
    val grade: String?,             // 等级（可选），如A/B/C/D
    val comment: String?,           // 评语（可选）
    val createdAt: Long = System.currentTimeMillis()
)
