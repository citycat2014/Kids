package com.example.kids.ui.academic.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kids.ui.academic.SemesterHelper

/**
 * 年级学期选择器 - 下拉框形式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSelector(
    currentGrade: String,
    selectedGrade: String,
    selectedSemester: String,
    availableGrades: List<String>,
    onGradeSelected: (String) -> Unit,
    onSemesterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 自动选中默认年级（优先当前年级，否则第一个）
    LaunchedEffect(selectedGrade, availableGrades, currentGrade) {
        if (selectedGrade.isBlank() && availableGrades.isNotEmpty()) {
            val default = currentGrade.takeIf { it.isNotBlank() && it in availableGrades }
                ?: availableGrades.first()
            onGradeSelected(default)
        }
    }

    // 自动选中默认学期
    LaunchedEffect(selectedSemester) {
        if (selectedSemester.isBlank()) {
            onSemesterSelected(SemesterHelper.getCurrentSemester())
        }
    }

    var gradeExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }

    val semesters = listOf("上学期", "下学期")

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // 年级下拉框
        ExposedDropdownMenuBox(
            expanded = gradeExpanded,
            onExpandedChange = { gradeExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedGrade,
                onValueChange = {},
                readOnly = true,
                label = { Text("年级") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = gradeExpanded,
                onDismissRequest = { gradeExpanded = false }
            ) {
                availableGrades.forEach { grade ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(grade.replace("小学", "").replace("初中", "").replace("高中", ""))
                                if (grade == currentGrade) {
                                    Text(
                                        text = "(当前)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onGradeSelected(grade)
                            gradeExpanded = false
                        }
                    )
                }
            }
        }

        // 学期下拉框
        ExposedDropdownMenuBox(
            expanded = semesterExpanded,
            onExpandedChange = { semesterExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedSemester,
                onValueChange = {},
                readOnly = true,
                label = { Text("学期") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = semesterExpanded,
                onDismissRequest = { semesterExpanded = false }
            ) {
                semesters.forEach { semester ->
                    DropdownMenuItem(
                        text = { Text(semester) },
                        onClick = {
                            onSemesterSelected(semester)
                            semesterExpanded = false
                        }
                    )
                }
            }
        }
    }
}