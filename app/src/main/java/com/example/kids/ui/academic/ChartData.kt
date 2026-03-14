package com.example.kids.ui.academic

import java.time.LocalDate

/**
 * 图表数据点 - 用于绘制成绩趋势图
 */
data class ChartDataPoint(
    val date: LocalDate,
    val subject: String,
    val score: Float,
    val examType: String,
    val examName: String
)

/**
 * 科目统计信息
 */
data class SubjectStatistics(
    val subject: String,
    val averageScore: Float,
    val highestScore: Float,
    val lowestScore: Float,
    val totalExams: Int,
    val trend: TrendDirection
)

/**
 * 趋势方向
 */
enum class TrendDirection {
    UP,      // 上升
    DOWN,    // 下降
    STABLE   // 持平
}

/**
 * 考试类型统计
 */
data class ExamTypeStatistics(
    val examType: String,
    val count: Int,
    val averageScore: Float
)

/**
 * 年级成绩概览
 */
data class GradeOverview(
    val grade: String,
    val totalExams: Int,
    val subjects: List<String>,
    val subjectStatistics: List<SubjectStatistics>
)

/**
 * 学期帮助类
 */
object SemesterHelper {
    // 根据日期判断学期：下学期(春季) 2月-8月，上学期(秋季) 9月-次年1月
    fun getSemesterFromDate(date: LocalDate): String {
        val month = date.monthValue
        return if (month in 2..8) {
            "下学期"
        } else {
            "上学期"
        }
    }

    // 获取当前学期
    fun getCurrentSemester(): String {
        return getSemesterFromDate(LocalDate.now())
    }
}
