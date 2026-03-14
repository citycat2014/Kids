package com.example.kids.ui.academic.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kids.data.model.SubjectConfig
import com.example.kids.ui.academic.ExamUi
import com.example.kids.ui.theme.AppleBackground
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")

/**
 * 成绩表格 - 展示各科成绩明细
 * 同一单元（单元测试）或同一日期（其他类型）的多科目会合并到一行展示
 */
@Composable
fun ScoreTable(
    exams: List<ExamUi>,
    modifier: Modifier = Modifier
) {
    if (exams.isEmpty()) {
        EmptyTable(modifier = modifier)
        return
    }

    // 获取所有科目（根据年级过滤）
    val allSubjects = remember(exams) {
        val subjects = exams.flatMap { it.records.map { r -> r.subject } }
            .distinct()
            .sorted()

        // 如果有记录，根据年级过滤科目
        val gradeLevel = exams.firstOrNull()?.gradeLevel ?: ""
        if (gradeLevel.contains("小学")) {
            // 小学只显示语数英
            subjects.filter { it in listOf("语文", "数学", "英语") }
        } else {
            subjects
        }
    }

    // 合并同一单元的考试（单元测试）或同一日期的考试（其他类型）
    val mergedExams = remember(exams) {
        exams.groupBy { exam ->
            if (exam.examType == "单元测试") {
                // 从 examName 中提取单元名称
                val unitName = extractUnitName(exam.examName)
                Pair(unitName, exam.examType)
            } else {
                Pair(exam.examDate.toString(), exam.examType)
            }
        }
            .map { (key, examList) ->
                val (groupKey, type) = key
                // 使用最早的日期作为该单元/分组的代表日期
                val representativeDate = examList.minOf { it.examDate }
                // 合并所有记录
                val allRecords = examList.flatMap { it.records }
                // 按科目去重（如果有重复，保留第一个）
                val uniqueRecords = allRecords.distinctBy { it.subject }
                // 计算平均分
                val averageScore = uniqueRecords
                    .mapNotNull { it.score }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?.toFloat()

                // 对于单元测试，提取单元名称
                val unitName = if (type == "单元测试") groupKey else null

                MergedExamUi(
                    examDate = representativeDate,
                    examType = type,
                    gradeLevel = examList.firstOrNull()?.gradeLevel ?: "",
                    records = uniqueRecords,
                    averageScore = averageScore,
                    unitName = unitName
                )
            }
            .sortedByDescending { it.examDate }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppleBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "成绩明细",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 表格容器 - 支持横向滚动
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // 表头
                    TableHeader(subjects = allSubjects)

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 表体
                    mergedExams.forEachIndexed { index, exam ->
                        MergedTableRow(
                            exam = exam,
                            subjects = allSubjects,
                            isEvenRow = index % 2 == 0
                        )
                        if (index < mergedExams.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 合并后的考试数据类
 * 对于单元测试，按单元名称分组而非日期
 */
private data class MergedExamUi(
    val examDate: java.time.LocalDate,
    val examType: String,
    val gradeLevel: String,
    val records: List<com.example.kids.ui.academic.AcademicRecordUi>,
    val averageScore: Float?,
    val unitName: String? = null  // 单元名称（仅单元测试使用）
)

/**
 * 从考试名称中提取单元名称
 * 例如："第一单元数学测试" -> "第一单元"
 */
private fun extractUnitName(examName: String): String {
    // 匹配常见的单元名称模式
    val patterns = listOf(
        "(第[一二三四五六七八九十]+单元)",
        "(Unit\\s*\\d+)",
        "(第\\d+单元)"
    )

    for (pattern in patterns) {
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        val match = regex.find(examName)
        if (match != null) {
            return match.groupValues[1]
        }
    }

    // 如果没有匹配到，返回原始考试名称
    return examName
}

@Composable
private fun TableHeader(subjects: List<String>) {
    val cellWidth = 80.dp
    val dateWidth = 70.dp
    val typeWidth = 80.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 日期
        Text(
            text = "日期",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(dateWidth),
            textAlign = TextAlign.Center
        )

        // 类型
        Text(
            text = "类型",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(typeWidth),
            textAlign = TextAlign.Center
        )

        // 科目
        subjects.forEach { subject ->
            Text(
                text = subject,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(cellWidth),
                textAlign = TextAlign.Center
            )
        }

        // 平均分
        Text(
            text = "平均",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(cellWidth),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MergedTableRow(
    exam: MergedExamUi,
    subjects: List<String>,
    isEvenRow: Boolean
) {
    val cellWidth = 80.dp
    val dateWidth = 70.dp
    val typeWidth = 80.dp

    val backgroundColor = if (isEvenRow) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 日期
        Text(
            text = exam.examDate.format(dateFormatter),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(dateWidth),
            textAlign = TextAlign.Center
        )

        // 类型（单元测试显示单元名称，其他显示考试类型）
        Text(
            text = exam.unitName ?: exam.examType,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(typeWidth),
            textAlign = TextAlign.Center
        )

        // 各科目成绩
        subjects.forEach { subject ->
            val record = exam.records.find { it.subject == subject }
            ScoreCell(
                score = record?.score,
                grade = record?.grade,
                modifier = Modifier.width(cellWidth)
            )
        }

        // 平均分
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(cellWidth)
        ) {
            Text(
                text = exam.averageScore?.let { "%.1f".format(it) } ?: "-",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScoreCell(
    score: Float?,
    grade: String?,
    modifier: Modifier = Modifier
) {
    val displayText = when {
        score != null -> "%.0f".format(score)
        grade != null -> grade
        else -> "-"
    }

    val backgroundColor = when {
        score == null && grade == null -> Color.Transparent
        score != null && score >= 90 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        score != null && score >= 80 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        score != null && score >= 60 -> MaterialTheme.colorScheme.surfaceVariant
        score != null -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        grade?.startsWith("A") == true -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        grade?.startsWith("B") == true -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        grade?.startsWith("C") == true -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    }

    val textColor = when {
        score == null && grade == null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        score != null && score >= 90 -> MaterialTheme.colorScheme.tertiary
        score != null && score >= 80 -> MaterialTheme.colorScheme.secondary
        score != null && score >= 60 -> MaterialTheme.colorScheme.onSurface
        score != null -> MaterialTheme.colorScheme.error
        grade?.startsWith("A") == true -> MaterialTheme.colorScheme.tertiary
        grade?.startsWith("B") == true -> MaterialTheme.colorScheme.secondary
        grade?.startsWith("C") == true -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.error
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = if (score != null && score >= 90) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyTable(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppleBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无成绩数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
