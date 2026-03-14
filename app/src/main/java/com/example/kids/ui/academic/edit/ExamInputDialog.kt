package com.example.kids.ui.academic.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kids.data.model.SubjectConfig
import com.example.kids.ui.academic.ExamUi
import com.example.kids.ui.academic.SemesterHelper
import com.example.kids.ui.academic.SubjectScoreInput
import com.example.kids.ui.components.DatePickerField
import java.time.LocalDate

/**
 * 考试录入弹窗 - 支持多科目和单科目模式
 * 年级直接使用孩子的当前年级，不可修改
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamInputDialog(
    kidGrade: String,  // 直接使用孩子的当前年级
    onDismiss: () -> Unit,
    onSaveMulti: (examType: String, examDate: LocalDate, gradeLevel: String, semester: String, records: List<SubjectScoreInput>) -> Unit,
    onSaveSingle: (examType: String, examDate: LocalDate, gradeLevel: String, semester: String, unit: String?, record: SubjectScoreInput) -> Unit,
    // 编辑模式参数
    editingExam: ExamUi? = null
) {
    // 初始化状态：编辑模式时预填充数据
    var examDate by remember { mutableStateOf(editingExam?.examDate ?: LocalDate.now()) }
    var examType by remember { mutableStateOf(editingExam?.examType ?: SubjectConfig.EXAM_TYPES[1]) }
    val selectedGrade = kidGrade  // 直接使用传入的年级，不可修改

    // 根据日期自动计算学期
    val semester = remember(examDate) {
        SemesterHelper.getSemesterFromDate(examDate)
    }

    // 是否多科目模式
    val isMultiSubject = SubjectConfig.isMultiSubjectMode(examType)

    // 根据年级获取科目列表
    val subjects = remember(selectedGrade) {
        SubjectConfig.getSubjectsByGrade(selectedGrade)
    }

    // 多科目模式状态：编辑模式时预填充现有成绩
    var subjectScores by remember { mutableStateOf<Map<String, SubjectScoreInput>>(emptyMap()) }

    // 单科目模式状态
    var selectedSubject by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("") }
    var singleScore by remember { mutableStateOf<Float?>(null) }
    var singleBonusScore by remember { mutableStateOf<Float?>(null) }
    var singleGrade by remember { mutableStateOf<String?>(null) }
    var singleGradeManuallySet by remember { mutableStateOf(false) }
    var singleComment by remember { mutableStateOf("") }

    // 初始化：编辑模式时预填充数据
    LaunchedEffect(editingExam, subjects, isMultiSubject) {
        val exam = editingExam
        if (exam != null) {
            if (isMultiSubject) {
                // 多科目模式：预填充所有科目成绩
                val existingRecords = exam.records.associateBy { it.subject }
                subjectScores = subjects.associateWith { subject ->
                    existingRecords[subject]?.let { record ->
                        SubjectScoreInput(
                            subject = subject,
                            score = record.score,
                            bonusScore = record.bonusScore,
                            grade = record.grade,
                            comment = record.comment,
                            gradeManuallySet = record.grade != null
                        )
                    } ?: SubjectScoreInput(subject, null, null, null, null)
                }
            } else {
                // 单科目模式：预填充第一个科目
                exam.records.firstOrNull()?.let { record ->
                    selectedSubject = record.subject
                    singleScore = record.score
                    singleBonusScore = record.bonusScore
                    singleGrade = record.grade
                    singleGradeManuallySet = record.grade != null
                    singleComment = record.comment ?: ""
                }
            }
        } else if (isMultiSubject) {
            // 新建模式：初始化所有科目
            subjectScores = subjects.associateWith { subject ->
                SubjectScoreInput(subject, null, null, null, null)
            }
        } else if (selectedSubject.isBlank() && subjects.isNotEmpty()) {
            selectedSubject = subjects.first()
        }
    }

    var examTypeExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var singleGradeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val titleText = if (editingExam != null) {
                if (isMultiSubject) "编辑期中/期末成绩" else "编辑单科成绩"
            } else {
                if (isMultiSubject) "添加期中/期末成绩" else "添加单科成绩"
            }
            Text(titleText)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 考试日期
                DatePickerField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    label = { Text("考试日期") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 考试类型
                ExposedDropdownMenuBox(
                    expanded = examTypeExpanded,
                    onExpandedChange = { examTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = examType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("考试类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = examTypeExpanded,
                        onDismissRequest = { examTypeExpanded = false }
                    ) {
                        SubjectConfig.EXAM_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    examType = type
                                    examTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 年级显示（只读，直接使用孩子资料中的年级）
                OutlinedTextField(
                    value = selectedGrade,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("年级") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 学期显示（根据日期自动计算）
                OutlinedTextField(
                    value = semester,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("学期（根据日期自动计算）") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isMultiSubject) {
                    // ===== 多科目录入模式 =====
                    Text(
                        text = "科目成绩",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    subjects.forEach { subject ->
                        MultiSubjectScoreRow(
                            subject = subject,
                            input = subjectScores[subject] ?: SubjectScoreInput(subject, null, null, null, null),
                            onInputChange = { input ->
                                subjectScores = subjectScores.toMutableMap().apply {
                                    put(subject, input)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    // ===== 单科目录入模式 =====
                    // 单元选择（用于单元测试等）
                    if (examType == "单元测试") {
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("单元") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                SubjectConfig.UNITS.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            selectedUnit = unit
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedUnit == "自定义") {
                            OutlinedTextField(
                                value = customUnit,
                                onValueChange = { customUnit = it },
                                label = { Text("自定义单元名称") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // 科目选择
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("科目") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        selectedSubject = subject
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 分数和等级
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = singleScore?.toString() ?: "",
                            onValueChange = { value ->
                                val score = value.toFloatOrNull()
                                singleScore = score
                                // 自动计算等级（如果用户未手动设置过）
                                if (score != null && !singleGradeManuallySet) {
                                    singleGrade = SubjectConfig.calculateGradeFromScore(score)
                                }
                            },
                            label = { Text("分数") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // 数学科目显示附加分输入框
                        if (selectedSubject == "数学") {
                            OutlinedTextField(
                                value = singleBonusScore?.toString() ?: "",
                                onValueChange = { value ->
                                    singleBonusScore = value.toFloatOrNull()
                                },
                                label = { Text("附加分") },
                                placeholder = { Text("可选") },
                                modifier = Modifier.weight(0.8f),
                                singleLine = true
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = singleGradeExpanded,
                            onExpandedChange = { singleGradeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = singleGrade ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("等级") },
                                placeholder = { Text("可选") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = singleGradeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = singleGradeExpanded,
                                onDismissRequest = { singleGradeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("(无)") },
                                    onClick = {
                                        singleGrade = null
                                        singleGradeManuallySet = true
                                        singleGradeExpanded = false
                                    }
                                )
                                SubjectConfig.GRADE_OPTIONS.forEach { grade ->
                                    DropdownMenuItem(
                                        text = { Text(grade) },
                                        onClick = {
                                            singleGrade = grade
                                            singleGradeManuallySet = true
                                            singleGradeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 备注
                    OutlinedTextField(
                        value = singleComment,
                        onValueChange = { singleComment = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isMultiSubject) {
                        val records = subjectScores.values.filter { it.score != null || !it.grade.isNullOrBlank() }
                        onSaveMulti(examType, examDate, selectedGrade, semester, records)
                    } else {
                        val unitValue = when {
                            examType != "单元测试" -> null
                            selectedUnit == "自定义" -> customUnit.takeIf { it.isNotBlank() }
                            else -> selectedUnit
                        }
                        val record = SubjectScoreInput(selectedSubject, singleScore, singleBonusScore, singleGrade, singleComment.takeIf { it.isNotBlank() })
                        onSaveSingle(examType, examDate, selectedGrade, semester, unitValue, record)
                    }
                },
                enabled = selectedGrade.isNotBlank() &&
                    if (isMultiSubject) subjectScores.values.any { it.score != null || !it.grade.isNullOrBlank() }
                    else selectedSubject.isNotBlank() && (singleScore != null || !singleGrade.isNullOrBlank())
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiSubjectScoreRow(
    subject: String,
    input: SubjectScoreInput,
    onInputChange: (SubjectScoreInput) -> Unit
) {
    var gradeExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = subject,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 分数输入
                OutlinedTextField(
                    value = input.score?.toString() ?: "",
                    onValueChange = { value ->
                        val score = value.toFloatOrNull()
                        // 自动计算等级（如果用户未手动设置过）
                        val autoGrade = if (score != null && !input.gradeManuallySet) {
                            SubjectConfig.calculateGradeFromScore(score)
                        } else {
                            input.grade
                        }
                        onInputChange(input.copy(score = score, grade = autoGrade))
                    },
                    label = { Text("分数") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // 数学科目显示附加分输入框
                if (subject == "数学") {
                    OutlinedTextField(
                        value = input.bonusScore?.toString() ?: "",
                        onValueChange = { value ->
                            val bonusScore = value.toFloatOrNull()
                            onInputChange(input.copy(bonusScore = bonusScore))
                        },
                        label = { Text("附加分") },
                        placeholder = { Text("可选") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                // 等级下拉
                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = input.grade ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("等级") },
                        placeholder = { Text("可选") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("(无)") },
                            onClick = {
                                onInputChange(input.copy(grade = null, gradeManuallySet = true))
                                gradeExpanded = false
                            }
                        )
                        SubjectConfig.GRADE_OPTIONS.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    onInputChange(input.copy(grade = grade, gradeManuallySet = true))
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
