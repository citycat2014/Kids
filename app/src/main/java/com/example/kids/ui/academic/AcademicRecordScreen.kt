package com.example.kids.ui.academic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kids.ui.academic.edit.AcademicEditScreen
import com.example.kids.ui.academic.preview.AcademicPreviewScreen

/**
 * 学习档案主界面 - 带Tab切换的预览/编辑模式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicRecordScreen(
    kidId: Long,
    initialGrade: String? = null,
    onBack: () -> Unit
) {
    val vm: AcademicRecordViewModel = viewModel()
    val state by vm.uiState.collectAsState()

    // Tab状态: 0=预览, 1=编辑
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // 加载数据
    LaunchedEffect(kidId) {
        vm.load(kidId, initialGrade)
    }

    // 错误弹窗
    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            title = { Text("错误") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = vm::clearError) {
                    Text("确定")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("${state.kidName}的学习档案") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                )

                // Tab切换
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("预览") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("编辑") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> AcademicPreviewScreen(
                state = state,
                onGradeSelected = vm::selectPreviewGrade,
                onSemesterSelected = vm::selectPreviewSemester,
                modifier = Modifier.padding(padding)
            )
            1 -> AcademicEditScreen(
                state = state,
                onGradeSelected = vm::selectEditGrade,
                onSemesterSelected = vm::selectEditSemester,
                onSaveExam = vm::saveExam,
                onSaveSingleRecord = vm::saveSingleRecord,
                onUpdateExam = { oldDate, oldExamName, examType, examDate, gradeLevel, semester, records ->
                    vm.updateExam(oldDate, oldExamName, examType, examDate, gradeLevel, semester, records)
                },
                onUpdateSingleRecord = { oldDate, oldExamName, examType, examDate, gradeLevel, semester, unit, record ->
                    vm.updateSingleRecord(oldDate, oldExamName, examType, examDate, gradeLevel, semester, unit, record)
                },
                onDeleteExam = vm::deleteExam,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
