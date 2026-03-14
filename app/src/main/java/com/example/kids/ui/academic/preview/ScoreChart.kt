package com.example.kids.ui.academic.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kids.ui.academic.ChartDataPoint
import com.example.kids.ui.theme.AppleBackground
import java.time.format.DateTimeFormatter

private val CHART_COLORS = listOf(
    Color(0xFF2196F3), // Blue
    Color(0xFF4CAF50), // Green
    Color(0xFFFF9800), // Orange
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFF5722), // Red
    Color(0xFF795548)  // Brown
)

private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

/**
 * 成绩趋势图 - 使用 Canvas 绘制折线图
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoreTrendChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    // 按科目分组
    val subjectGroups = remember(data) {
        data.groupBy { it.subject }
    }

    // 获取所有日期（X轴）
    val allDates = remember(data) {
        data.map { it.date }.distinct().sorted()
    }

    if (subjectGroups.isEmpty() || allDates.isEmpty()) {
        EmptyChart(modifier = modifier)
        return
    }

    // 科目颜色映射
    val subjectColors = remember(subjectGroups.keys.toList()) {
        subjectGroups.keys.toList().associateWith { subject ->
            val index = subjectGroups.keys.indexOf(subject)
            CHART_COLORS[index % CHART_COLORS.size]
        }
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
                text = "成绩趋势",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "最近${allDates.size}次考试",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 图表
            ChartCanvas(
                data = data,
                subjectGroups = subjectGroups,
                allDates = allDates,
                subjectColors = subjectColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 图例
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                subjectGroups.forEach { (subject, points) ->
                    LegendItem(
                        subject = subject,
                        color = subjectColors[subject] ?: Color.Gray,
                        dataPoints = points.size
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCanvas(
    data: List<ChartDataPoint>,
    subjectGroups: Map<String, List<ChartDataPoint>>,
    allDates: List<java.time.LocalDate>,
    subjectColors: Map<String, Color>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = with(density) { 32.dp.toPx() }
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // 绘制背景网格
        val gridColor = Color.LightGray.copy(alpha = 0.3f)
        val ySteps = 5 // 0, 20, 40, 60, 80, 100

        // Y轴网格线和标签
        for (i in 0..ySteps) {
            val y = padding + chartHeight - (i.toFloat() / ySteps) * chartHeight
            val score = (i * 20).toFloat()

            // 网格线
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )

            // Y轴标签
            val textLayoutResult = textMeasurer.measure(
                text = score.toInt().toString(),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(padding - textLayoutResult.size.width - 4, y - textLayoutResult.size.height / 2)
            )
        }

        // 绘制各科目折线
        subjectGroups.forEach { (subject, points) ->
            val color = subjectColors[subject] ?: Color.Gray
            val sortedPoints = points.sortedBy { it.date }

            // 计算坐标点
            val coordinates = sortedPoints.map { point ->
                val xIndex = allDates.indexOf(point.date)
                val x = if (allDates.size > 1) {
                    padding + (xIndex.toFloat() / (allDates.size - 1)) * chartWidth
                } else {
                    padding + chartWidth / 2
                }
                val y = padding + chartHeight - (point.score / 100f) * chartHeight
                Offset(x, y)
            }

            // 绘制折线
            if (coordinates.size > 1) {
                for (i in 0 until coordinates.size - 1) {
                    drawLine(
                        color = color,
                        start = coordinates[i],
                        end = coordinates[i + 1],
                        strokeWidth = 2.5f
                    )
                }
            }

            // 绘制数据点
            coordinates.forEach { point ->
                drawCircle(
                    color = color,
                    radius = 5f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = point
                )
            }
        }

        // X轴标签（只显示部分避免重叠）
        val labelStep = if (allDates.size <= 6) 1 else allDates.size / 5 + 1
        allDates.filterIndexed { index, _ -> index % labelStep == 0 }.forEach { date ->
            val xIndex = allDates.indexOf(date)
            val x = if (allDates.size > 1) {
                padding + (xIndex.toFloat() / (allDates.size - 1)) * chartWidth
            } else {
                padding + chartWidth / 2
            }

            val textLayoutResult = textMeasurer.measure(
                text = date.format(dateFormatter),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x - textLayoutResult.size.width / 2, height - padding + 4)
            )
        }
    }
}

@Composable
private fun LegendItem(
    subject: String,
    color: Color,
    dataPoints: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$subject($dataPoints)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EmptyChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppleBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
