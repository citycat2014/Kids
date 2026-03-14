package com.example.kids.ui.academic.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kids.ui.academic.AcademicRecordUiState
import com.example.kids.ui.academic.ExamUi
import com.example.kids.ui.academic.SubjectScoreInput
import com.example.kids.ui.academic.preview.GradeSelector
import com.example.kids.ui.theme.AppleBackground
import java.time.LocalDate

/**
 * 编辑模式主界面
 */
@Composable
fun AcademicEditScreen(
    state: AcademicRecordUiState,
    onGradeSelected: (String) -> Unit,
    onSemesterSelected: (String) -> Unit,
    onSaveExam: (examType: String, examDate: LocalDate, gradeLevel: String, semester: String, records: List<SubjectScoreInput>) -> Unit,
    onSaveSingleRecord: (examType: String, examDate: LocalDate, gradeLevel: String, semester: String, unit: String?, record: SubjectScoreInput) -> Unit,
    onUpdateExam: (oldDate: LocalDate, oldExamName: String, examType: String, examDate: LocalDate, gradeLevel: String, semester: String, records: List<SubjectScoreInput>) -> Unit,
    onUpdateSingleRecord: (oldDate: LocalDate, oldExamName: String, examType: String, examDate: LocalDate, gradeLevel: String, semester: String, unit: String?, record: SubjectScoreInput) -> Unit,
    onDeleteExam: (LocalDate, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var examToDelete by remember { mutableStateOf<ExamUi?>(null) }
    var examToEdit by remember { mutableStateOf<ExamUi?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加考试"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleBackground)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 年级选择器
            GradeSelector(
                currentGrade = state.kidGrade,
                selectedGrade = state.selectedEditGrade,
                selectedSemester = state.selectedEditSemester,
                availableGrades = state.availableGrades,
                onGradeSelected = onGradeSelected,
                onSemesterSelected = onSemesterSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 考试列表
            ExamList(
                exams = state.exams,
                onEditExam = { exam -> examToEdit = exam },
                onDeleteExam = { exam -> examToDelete = exam },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // 添加弹窗
    if (showAddDialog) {
        ExamInputDialog(
            kidGrade = state.selectedEditGrade,  // 使用当前选中的年级
            onDismiss = { showAddDialog = false },
            onSaveMulti = { examType, examDate, gradeLevel, semester, records ->
                onSaveExam(examType, examDate, gradeLevel, semester, records)
                showAddDialog = false
            },
            onSaveSingle = { examType, examDate, gradeLevel, semester, unit, record ->
                onSaveSingleRecord(examType, examDate, gradeLevel, semester, unit, record)
                showAddDialog = false
            }
        )
    }

    // 编辑弹窗
    if (examToEdit != null) {
        val editingExam = examToEdit!!
        ExamInputDialog(
            kidGrade = state.selectedEditGrade,
            onDismiss = { examToEdit = null },
            onSaveMulti = { examType, examDate, gradeLevel, semester, records ->
                onUpdateExam(editingExam.examDate, editingExam.examName, examType, examDate, gradeLevel, semester, records)
                examToEdit = null
            },
            onSaveSingle = { examType, examDate, gradeLevel, semester, unit, record ->
                onUpdateSingleRecord(editingExam.examDate, editingExam.examName, examType, examDate, gradeLevel, semester, unit, record)
                examToEdit = null
            },
            editingExam = editingExam
        )
    }

    // 删除确认弹窗
    if (examToDelete != null) {
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这次考试的所有成绩记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    examToDelete?.let { exam ->
                        onDeleteExam(exam.examDate, exam.examName)
                    }
                    examToDelete = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}
