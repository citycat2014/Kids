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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kids.ui.mood.KidMood
import com.example.kids.ui.mood.MoodCalendarViewModel
import com.example.kids.ui.mood.ExerciseType
import com.example.kids.ui.mood.ExerciseInfo
import com.example.kids.ui.utils.collectAsStateWithLifecycleSafe
import com.example.kids.ui.theme.AppleBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        onSetExercise = { date, type, duration ->
            vm.setExercise(date, type, duration)
        }
    )
}

@Composable
private fun MoodCalendarContent(
    month: YearMonth,
    moods: Map<LocalDate, KidMood>,
    exercises: Map<LocalDate, ExerciseInfo>,
    today: LocalDate,
    onBack: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMood: (LocalDate, KidMood?) -> Unit,
    onSetExercise: (LocalDate, ExerciseType?, Int?) -> Unit
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
        val existingExercise = exercises[targetDate]
        val exerciseInfoType = existingExercise?.type
        val exerciseInfo = existingExercise
        MoodSelectDialog(
            date = targetDate,
            today = today,
            existingMood = moods[targetDate],
            existingExercise = exerciseInfo,
            onDismiss = { dialogDate = null },
            onSelect = { moodOrNull ->
                onSelectMood(targetDate, moodOrNull)
                dialogDate = null
            },
            onSetExercise = { type, duration ->
                onSetExercise(targetDate, type, duration)
            }
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
    exercises: Map<LocalDate, ExerciseInfo>,
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
                        val exercise = exercises[date]
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
                                if (exercise?.type != null) {
                                    Text(
                                        text = " ${exercise.type.displayName.substring(0..1)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
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
    existingExercise: ExerciseInfo?,
    onDismiss: () -> Unit,
    onSelect: (KidMood?) -> Unit,
    onSetExercise: (ExerciseType?, Int?) -> Unit
) {
    var showExercise by remember { mutableStateOf(false) }
    var selectedExerciseType by remember { mutableStateOf(existingExercise?.type) }
    var exerciseDuration by remember { mutableStateOf(existingExercise?.durationMinutes?.toString().orEmpty()) }
    var exerciseError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mood buttons
                Button(onClick = { onSelect(KidMood.GOOD) }, modifier = Modifier.fillMaxWidth()) {
                    Text("乖")
                }
                Button(onClick = { onSelect(KidMood.OK) }, modifier = Modifier.fillMaxWidth()) {
                    Text("一般")
                }
                Button(onClick = { onSelect(KidMood.BAD) }, modifier = Modifier.fillMaxWidth()) {
                    Text("不乖")
                }

                // Exercise section
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("锻炼记录")
                    TextButton(
                        onClick = {
                            showExercise = !showExercise
                            if (!showExercise) {
                                onSetExercise(null, null)
                                selectedExerciseType = null
                                exerciseDuration = ""
                                exerciseError = null
                            }
                        }
                    ) {
                        Text(if (showExercise) "隐藏" else "添加")
                    }
                }

                if (showExercise) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Exercise type selection
                    androidx.compose.material3.DropdownMenu(
                        expanded = selectedExerciseType == null,
                        onDismissRequest = { selectedExerciseType = null }
                    ) {
                        ExerciseType.entries.forEach { type ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedExerciseType = type
                                    exerciseError = null
                                }
                            )
                        }
                    }

                    if (selectedExerciseType != null) {
                        OutlinedButton(
                            onClick = { selectedExerciseType = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("选择：${selectedExerciseType!!.displayName}")
                        }

                        Spacer(modifier = Modifier.height(4.dp))
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

                        if (exerciseError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exerciseError.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val duration = exerciseDuration.toIntOrNull()
                                if (duration == null || duration <= 0) {
                                    exerciseError = "请输入有效的锻炼时长"
                                } else {
                                    onSetExercise(selectedExerciseType, duration)
                                    selectedExerciseType = null
                                    exerciseDuration = ""
                                    exerciseError = null
                                    showExercise = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("保存锻炼记录")
                        }
                    }
                }

                if (existingMood != null && date.isAfter(today)) {
                    Button(
                        onClick = { onSelect(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("删除记录")
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


