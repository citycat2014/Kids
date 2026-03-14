package com.example.kids.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kids.R
import com.example.kids.ui.mood.KidMood
import com.example.kids.ui.mood.MoodCalendarViewModel
import com.example.kids.ui.mood.ExerciseType
import com.example.kids.ui.mood.ExerciseInfo
import com.example.kids.ui.utils.collectAsStateWithLifecycleSafe
import com.example.kids.ui.theme.AppleBackground
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


@Composable
fun MoodCalendarScreen(
    kidId: Long,
    onBack: () -> Unit
) {
    val vm: MoodCalendarViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycleSafe()

    LaunchedEffect(kidId) {
        vm.load(kidId)
    }

    MoodCalendarContent(
        month = state.month,
        moods = state.moods,
        exercises = state.exercises,
        today = LocalDate.now(),
        onBack = onBack,
        onPrevMonth = { vm.setMonth(state.month.minusMonths(1)) },
        onNextMonth = { vm.setMonth(state.month.plusMonths(1)) },
        onSelectMood = { date, moodOrNull ->
            if (moodOrNull == null) {
                vm.clearMood(date)
            } else {
                vm.setMood(date, moodOrNull)
            }
        },
        onAddExercise = { date, type, duration ->
            vm.addExercise(date, type, duration)
        },
        onRemoveExercise = { exerciseId ->
            vm.removeExercise(exerciseId)
        },
        onUpdateExercise = { exerciseId, type, duration ->
            vm.updateExercise(exerciseId, type, duration)
        }
    )
}

@Composable
private fun MoodCalendarContent(
    month: YearMonth,
    moods: Map<LocalDate, KidMood>,
    exercises: Map<LocalDate, List<ExerciseInfo>>,
    today: LocalDate,
    onBack: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMood: (LocalDate, KidMood?) -> Unit,
    onAddExercise: (LocalDate, ExerciseType, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onUpdateExercise: (Long, ExerciseType, Int) -> Unit
) {
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "乖不乖日历",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = onBack) {
                    Text("返回")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPrevMonth) {
                    Text("上个月")
                }
                Text(
                    text = month.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onNextMonth) {
                    Text("下个月")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            WeekHeaderRow()
            Spacer(modifier = Modifier.height(8.dp))
            CalendarGrid(
                month = month,
                moods = moods,
                exercises = exercises,
                today = today,
                onDayClick = { dialogDate = it }
            )
        }
    }

    val targetDate = dialogDate
    if (targetDate != null) {
        MoodSelectDialog(
            date = targetDate,
            today = today,
            existingMood = moods[targetDate],
            existingExercises = exercises[targetDate] ?: emptyList(),
            onDismiss = { dialogDate = null },
            onSelect = { moodOrNull ->
                onSelectMood(targetDate, moodOrNull)
            },
            onAddExercise = { type, duration ->
                onAddExercise(targetDate, type, duration)
            },
            onRemoveExercise = onRemoveExercise,
            onUpdateExercise = onUpdateExercise
        )
    }
}

@Composable
private fun WeekHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
            BoxDay(
                modifier = Modifier.weight(1f),
                content = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    moods: Map<LocalDate, KidMood>,
    exercises: Map<LocalDate, List<ExerciseInfo>>,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val lastDayOfMonth = month.atEndOfMonth()
    val firstDayOfWeekIndex = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // 以周一为0
    val daysInMonth = lastDayOfMonth.dayOfMonth

    val totalCells = firstDayOfWeekIndex + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var currentDay = 1
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(7) { column ->
                    val cellIndex = row * 7 + column
                    if (cellIndex < firstDayOfWeekIndex || currentDay > daysInMonth) {
                        BoxDay(modifier = Modifier.weight(1f)) {}
                    } else {
                        val date = month.atDay(currentDay)
                        val mood = moods[date]
                        val exercisesForDay = exercises[date]
                        val totalExerciseMinutes = exercisesForDay?.sumOf { it.durationMinutes } ?: 0
                        val editable = !date.isBefore(today)
                        BoxDay(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .let { base ->
                                    if (editable) {
                                        base.clickable { onDayClick(date) }
                                    } else {
                                        base
                                    }
                                },
                            backgroundColor = when (mood) {
                                KidMood.GOOD -> Color(0xFF4CAF50)
                                KidMood.OK -> Color(0xFFFFC107)
                                KidMood.BAD -> Color(0xFFF44336)
                                null -> Color.Transparent
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentDay.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (mood == null) {
                                        MaterialTheme.colorScheme.onBackground
                                    } else {
                                        Color.White
                                    }
                                )
                                if (totalExerciseMinutes > 0) {
                                    Text(
                                        text = " ${totalExerciseMinutes}分",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (mood == null) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.White.copy(alpha = 0.8f)
                                        },
                                        modifier = Modifier.padding(start = 1.dp)
                                    )
                                }
                            }
                        }
                        currentDay++
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxDay(
    modifier: Modifier,
    backgroundColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .height(40.dp)
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun MoodSelectDialog(
    date: LocalDate,
    today: LocalDate,
    existingMood: KidMood?,
    existingExercises: List<ExerciseInfo>,
    onDismiss: () -> Unit,
    onSelect: (KidMood?) -> Unit,
    onAddExercise: (ExerciseType, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onUpdateExercise: (Long, ExerciseType, Int) -> Unit
) {
    var showCustomExercise by remember { mutableStateOf(false) }
    var selectedExerciseType by remember { mutableStateOf<ExerciseType?>(null) }
    var exerciseDuration by remember { mutableStateOf("") }
    var exerciseError by remember { mutableStateOf<String?>(null) }

    // Edit mode states
    var editingExercise by remember { mutableStateOf<ExerciseInfo?>(null) }
    var editType by remember { mutableStateOf<ExerciseType?>(null) }
    var editDuration by remember { mutableStateOf("") }
    var editError by remember { mutableStateOf<String?>(null) }

    // Multi-select mode states
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // Quick exercise presets
    data class QuickExercise(val type: ExerciseType, val duration: Int, val label: String)
    val quickExercises = listOf(
        QuickExercise(ExerciseType.RUNNING, 30, "跑步 30分钟"),
        QuickExercise(ExerciseType.JUMP_ROPE, 15, "跳绳 15分钟"),
        QuickExercise(ExerciseType.BADMINTON, 60, "羽毛球 60分钟")
    )

    val totalExerciseMinutes = existingExercises.sumOf { it.durationMinutes }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mood section
                item {
                    Text(
                        text = "心情",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSelect(KidMood.GOOD) },
                            modifier = Modifier.weight(1f),
                            colors = if (existingMood == KidMood.GOOD) {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            } else {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("乖")
                        }
                        OutlinedButton(
                            onClick = { onSelect(KidMood.OK) },
                            modifier = Modifier.weight(1f),
                            colors = if (existingMood == KidMood.OK) {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFFFC107).copy(alpha = 0.1f)
                                )
                            } else {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("一般")
                        }
                        OutlinedButton(
                            onClick = { onSelect(KidMood.BAD) },
                            modifier = Modifier.weight(1f),
                            colors = if (existingMood == KidMood.BAD) {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                                )
                            } else {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("不乖")
                        }
                    }
                }

                // Exercise section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "今日运动",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (totalExerciseMinutes > 0) {
                            Text(
                                text = "共${totalExerciseMinutes}分钟",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Show existing exercises list
                if (existingExercises.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isMultiSelectMode) "已选择 ${selectedIds.size} 项" else "运动记录",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isMultiSelectMode) {
                                    TextButton(
                                        onClick = {
                                            selectedIds.forEach { onRemoveExercise(it) }
                                            selectedIds = emptySet()
                                            isMultiSelectMode = false
                                        },
                                        enabled = selectedIds.isNotEmpty()
                                    ) {
                                        Text(
                                            "删除选中",
                                            color = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    }
                                    TextButton(onClick = {
                                        selectedIds = emptySet()
                                        isMultiSelectMode = false
                                    }) {
                                        Text("取消")
                                    }
                                } else {
                                    TextButton(onClick = { isMultiSelectMode = true }) {
                                        Text("多选")
                                    }
                                }
                            }
                        }
                    }
                    items(existingExercises) { exercise ->
                        // Check if this exercise is being edited
                        if (editingExercise?.id == exercise.id) {
                            // Edit mode for this exercise
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ExerciseType.entries.forEach { type ->
                                        OutlinedButton(
                                            onClick = {
                                                editType = type
                                                editError = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = if (editType == type) {
                                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                )
                                            } else {
                                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                                            }
                                        ) {
                                            Text(
                                                text = type.displayName,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = editDuration,
                                    onValueChange = {
                                        editDuration = it
                                        editError = null
                                    },
                                    label = { Text("锻炼时长（分钟）") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (editError != null) {
                                    Text(
                                        text = editError.orEmpty(),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            editingExercise = null
                                            editType = null
                                            editDuration = ""
                                            editError = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("取消")
                                    }
                                    Button(
                                        onClick = {
                                            val duration = editDuration.toIntOrNull()
                                            if (duration == null || duration <= 0) {
                                                editError = "请输入有效的锻炼时长"
                                            } else if (editType == null) {
                                                editError = "请选择运动类型"
                                            } else {
                                                onUpdateExercise(exercise.id, editType!!, duration)
                                                editingExercise = null
                                                editType = null
                                                editDuration = ""
                                                editError = null
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("保存")
                                    }
                                }
                            }
                        } else {
                            // Normal display mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { base ->
                                        if (isMultiSelectMode) {
                                            base.clickable {
                                                selectedIds = if (exercise.id in selectedIds) {
                                                    selectedIds - exercise.id
                                                } else {
                                                    selectedIds + exercise.id
                                                }
                                            }
                                        } else {
                                            base
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isMultiSelectMode) {
                                        androidx.compose.material3.Checkbox(
                                            checked = exercise.id in selectedIds,
                                            onCheckedChange = { checked ->
                                                selectedIds = if (checked) {
                                                    selectedIds + exercise.id
                                                } else {
                                                    selectedIds - exercise.id
                                                }
                                            }
                                        )
                                    }
                                    Text(
                                        text = "${exercise.type.displayName} ${exercise.durationMinutes}分钟",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (!isMultiSelectMode) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingExercise = exercise
                                                editType = exercise.type
                                                editDuration = exercise.durationMinutes.toString()
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                                contentDescription = "编辑",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = { onRemoveExercise(exercise.id) }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                                contentDescription = "删除",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick exercise buttons
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickExercises.forEach { quick ->
                            OutlinedButton(
                                onClick = {
                                    onAddExercise(quick.type, quick.duration)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(quick.label)
                            }
                        }
                    }
                }

                // Custom exercise button
                item {
                    TextButton(
                        onClick = {
                            showCustomExercise = !showCustomExercise
                            if (!showCustomExercise) {
                                selectedExerciseType = null
                                exerciseDuration = ""
                                exerciseError = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showCustomExercise) "收起自定义" else "自定义运动...")
                    }
                }

                // Custom exercise form
                if (showCustomExercise) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "选择运动类型：",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Exercise type buttons in rows of 3
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ExerciseType.entries.chunked(3).forEach { rowTypes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowTypes.forEach { type ->
                                        OutlinedButton(
                                            onClick = {
                                                selectedExerciseType = type
                                                exerciseError = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = if (selectedExerciseType == type) {
                                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                )
                                            } else {
                                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                                            }
                                        ) {
                                            Text(
                                                text = type.displayName,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    // Fill remaining space if row is not full
                                    repeat(3 - rowTypes.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Duration input and save button
                    if (selectedExerciseType != null) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = exerciseDuration,
                                onValueChange = {
                                    exerciseDuration = it
                                    exerciseError = null
                                },
                                label = { Text("锻炼时长（分钟）") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (exerciseError != null) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exerciseError.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val duration = exerciseDuration.toIntOrNull()
                                    if (duration == null || duration <= 0) {
                                        exerciseError = "请输入有效的锻炼时长"
                                    } else {
                                        onAddExercise(selectedExerciseType!!, duration)
                                        showCustomExercise = false
                                        selectedExerciseType = null
                                        exerciseDuration = ""
                                        exerciseError = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("添加")
                            }
                        }
                    }
                }

                // Delete record option (for today and future dates)
                if (existingMood != null && !date.isBefore(today)) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { onSelect(null) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "删除当天记录",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}