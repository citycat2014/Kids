package com.example.kids.ui.academic.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kids.ui.academic.AcademicRecordUi
import com.example.kids.ui.academic.ExamUi
import com.example.kids.ui.theme.AppleBackground
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/**
 * 考试列表 - 编辑模式下的列表展示
 */
@Composable
fun ExamList(
    exams: List<ExamUi>,
    onEditExam: (ExamUi) -> Unit,
    onDeleteExam: (ExamUi) -> Unit,
    modifier: Modifier = Modifier
) {
    if (exams.isEmpty()) {
        EmptyExamList(modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exams, key = { "${it.examDate}_${it.examName}" }) { exam ->
            ExamCard(
                exam = exam,
                onEdit = { onEditExam(exam) },
                onDelete = { onDeleteExam(exam) }
            )
        }
    }
}

@Composable
private fun ExamCard(
    exam: ExamUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    // 解析单元信息
    val unitInfo = remember(exam.examName) {
        extractUnitFromName(exam.examName)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.examName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(exam.examDate.format(dateFormatter))
                            if (unitInfo != null) {
                                append(" · ")
                                append(unitInfo)
                            }
                            append(" · ")
                            append(exam.examType)
                            append(" · ")
                            append(exam.gradeLevel)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row {
                    // 平均分显示
                    exam.averageScore?.let { avg ->
                        Surface(
                            color = getScoreColor(avg).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "均分: %.1f".format(avg),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = getScoreColor(avg)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // 编辑按钮
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑"
                        )
                    }

                    // 展开/收起按钮
                    IconButton(onClick = { showDetails = !showDetails }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = if (showDetails) "收起" else "展开详情"
                        )
                    }

                    // 删除按钮
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 展开显示详情
            if (showDetails) {
                Spacer(modifier = Modifier.height(12.dp))

                exam.records.forEach { record ->
                    RecordItem(record = record)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RecordItem(record: AcademicRecordUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.subject,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分数
                record.score?.let { score ->
                    Text(
                        text = "%.1f分".format(score),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = getScoreColor(score)
                    )
                }

                // 等级
                record.grade?.let { grade ->
                    Surface(
                        color = when (grade.firstOrNull()) {
                            'A' -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            'B' -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            'C' -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = grade,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 备注
        record.comment?.takeIf { it.isNotBlank() }?.let { comment ->
            Text(
                text = comment,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EmptyExamList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "暂无成绩记录",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮添加考试成绩",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// 从考试名称中提取单元信息
private fun extractUnitFromName(examName: String): String? {
    val unitPattern = Regex("^(第[一二三四五六七八]单元)")
    return unitPattern.find(examName)?.groupValues?.get(1)
}

// 根据分数获取颜色
private fun getScoreColor(score: Float) = when {
    score >= 90 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
    score >= 80 -> androidx.compose.ui.graphics.Color(0xFF2196F3)
    score >= 60 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
    else -> androidx.compose.ui.graphics.Color(0xFFE91E63)
}
