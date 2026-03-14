package com.example.kids.ui.academic.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kids.ui.academic.AcademicRecordUiState
import com.example.kids.ui.theme.AppleBackground

/**
 * 视图模式
 */
enum class ViewMode {
    CHART,  // 图表模式
    TABLE   // 表格模式
}

/**
 * 预览模式主界面
 */
@Composable
fun AcademicPreviewScreen(
    state: AcademicRecordUiState,
    onGradeSelected: (String) -> Unit,
    onSemesterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(ViewMode.CHART) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 年级选择器（带学期选择）
        GradeSelector(
            currentGrade = state.kidGrade,
            selectedGrade = state.selectedPreviewGrade,
            selectedSemester = state.selectedPreviewSemester,
            availableGrades = state.availableGrades,
            onGradeSelected = onGradeSelected,
            onSemesterSelected = onSemesterSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.exams.isEmpty()) {
            // 空状态
            EmptyPreviewState()
        } else {
            // 视图模式切换
            ViewModeSwitcher(
                currentMode = viewMode,
                onModeChange = { viewMode = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 根据模式显示不同内容
            when (viewMode) {
                ViewMode.CHART -> {
                    // 成绩趋势图
                    ScoreTrendChart(
                        data = state.chartData,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 统计卡片
                    StatisticsCards(
                        subjectStatistics = state.subjectStatistics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ViewMode.TABLE -> {
                    // 成绩表格
                    ScoreTable(
                        exams = state.exams,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 视图模式切换器
 */
@Composable
private fun ViewModeSwitcher(
    currentMode: ViewMode,
    onModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "视图模式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(end = 8.dp)
        )

        // 图表模式按钮
        IconButton(
            onClick = { onModeChange(ViewMode.CHART) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (currentMode == ViewMode.CHART) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "图表模式",
                tint = if (currentMode == ViewMode.CHART) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 表格模式按钮
        IconButton(
            onClick = { onModeChange(ViewMode.TABLE) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (currentMode == ViewMode.TABLE) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "表格模式",
                tint = if (currentMode == ViewMode.TABLE) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Composable
private fun EmptyPreviewState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(AppleBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "暂无成绩数据",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "切换到\"编辑\"标签添加考试成绩",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
