package com.example.kids.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日期选择器组件
 * 提供友好的日期选择界面，替代手动输入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    OutlinedTextField(
        value = value?.format(dateFormatter) ?: "",
        onValueChange = { /* 不允许手动输入 */ },
        label = label,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = true,
        isError = isError,
        supportingText = supportingText ?: if (errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        singleLine = true,
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) {
                Text("选择")
            }
        }
    )

    if (showPicker) {
        // 使用 mutableStateOf 存储选中的日期，在用户点击确定时使用
        var selectedDateState by remember { mutableStateOf<LocalDate?>(null) }

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        showPicker = false
                        // 只有在用户点击确定且日期真正改变时才更新值
                        if (selectedDateState != null && selectedDateState != value) {
                            onValueChange(selectedDateState)
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )

            DatePicker(state = datePickerState)

            // 监听日期变化，更新状态但不立即回调
            val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
                Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

            // 记录dialog打开时的初始值，用于比较
            val initialValue = remember { value }

            // 只在用户选择新日期时更新状态（非确认阶段）
            if (selectedDate != null && selectedDate != initialValue) {
                selectedDateState = selectedDate
            }
        }
    }
}

/**
 * 带清除功能的日期选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerFieldWithClear(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "点击选择日期"
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    OutlinedTextField(
        value = value?.format(dateFormatter) ?: "",
        onValueChange = { /* 不允许手动输入 */ },
        label = label,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = true,
        singleLine = true,
        placeholder = { Text(placeholder) },
        trailingIcon = {
            if (value != null) {
                TextButton(onClick = { onValueChange(null) }) {
                    Text("清除")
                }
            } else {
                TextButton(onClick = { showPicker = true }) {
                    Text("选择")
                }
            }
        }
    )

    if (showPicker) {
        // 使用 mutableStateOf 存储选中的日期，在用户点击确定时使用
        var selectedDateState by remember { mutableStateOf<LocalDate?>(null) }

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        showPicker = false
                        // 只有在用户点击确定且日期真正改变时才更新值
                        if (selectedDateState != null && selectedDateState != value) {
                            onValueChange(selectedDateState)
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )

            DatePicker(state = datePickerState)

            // 监听日期变化，更新状态但不立即回调
            val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
                Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

            // 记录dialog打开时的初始值，用于比较
            val initialValue = remember { value }

            // 只在用户选择新日期时更新状态（非确认阶段）
            if (selectedDate != null && selectedDate != initialValue) {
                selectedDateState = selectedDate
            }
        }
    }
}