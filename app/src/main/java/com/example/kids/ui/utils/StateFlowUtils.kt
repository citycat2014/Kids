package com.example.kids.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow

/**
 * Helper function to collect StateFlow with lifecycle without lifecycle-compose dependency
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleSafe(): State<T> {
    val flow = this
    val state = remember { mutableStateOf(flow.value) }
    LaunchedEffect(flow) {
        withContext(Dispatchers.Main.immediate) {
            flow.collect { state.value = it }
        }
    }
    return state
}