package com.example.kids.ui.academic.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kids.ui.academic.SubjectStatistics
import com.example.kids.ui.academic.TrendDirection
import com.example.kids.ui.theme.AppleBackground

/**
 * 统计卡片区域 - 展示各科统计信息
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsCards(
    subjectStatistics: List<SubjectStatistics>,
    modifier: Modifier = Modifier
) {
    if (subjectStatistics.isEmpty()) return

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
                text = "科目统计",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                subjectStatistics.forEach { stats ->
                    SubjectStatCard(statistics = stats)
                }
            }
        }
    }
}

@Composable
private fun SubjectStatCard(
    statistics: SubjectStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 科目名和趋势
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statistics.subject,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                TrendIndicator(trend = statistics.trend)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 平均分
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "%.1f".format(statistics.averageScore),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(statistics.averageScore)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "平均分",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 最高分和考试次数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "最高: %.0f".format(statistics.highestScore),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${statistics.totalExams}次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TrendIndicator(trend: TrendDirection) {
    val (symbol, color) = when (trend) {
        TrendDirection.UP -> Pair("↑", Color(0xFF4CAF50))
        TrendDirection.DOWN -> Pair("↓", Color(0xFFE91E63))
        TrendDirection.STABLE -> Pair("—", Color(0xFF757575))
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = symbol,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun getScoreColor(score: Float): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50)
        score >= 80 -> Color(0xFF2196F3)
        score >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFE91E63)
    }
}
