package com.example.kids.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kids.ui.growth.GrowthRecordViewModel
import com.example.kids.ui.theme.AppleBackground
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@Composable
fun GrowthTimelineScreen(
    kidId: Long,
    onBack: () -> Unit
) {
    val vm: GrowthRecordViewModel = viewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(kidId) {
        vm.load(kidId)
    }

    val photos = state.records
        .filter { !it.photoUri.isNullOrBlank() }
        .sortedBy { it.date }

    GrowthTimelineContent(
        photos = photos,
        onBack = onBack
    )
}

@Composable
private fun GrowthTimelineContent(
    photos: List<com.example.kids.ui.growth.GrowthRecordUi>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "成长影像",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "按时间自动轮播带照片的记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            TextButton(onClick = onBack) {
                Text("返回")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有带照片的成长记录\n去添加几条带照片的记录吧～",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            return
        }

        var currentIndex by remember { mutableIntStateOf(0) }
        var isPlaying by remember { mutableStateOf(true) }

        LaunchedEffect(photos.size, isPlaying) {
            while (true) {
                if (isPlaying && photos.isNotEmpty()) {
                    delay(2500)
                    currentIndex = (currentIndex + 1) % photos.size
                } else {
                    delay(200)
                }
            }
        }

        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(500)
                    ) + fadeIn(tween(500))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                animationSpec = tween(500)
                            ) + fadeOut(tween(500)))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
            ) { index ->
                val frame = photos.getOrNull(index) ?: photos.first()
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(frame.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "成长影像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val current = photos.getOrNull(currentIndex) ?: photos.first()

            Text(
                text = current.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.titleMedium
            )
            if (!current.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = current.note ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isPlaying = !isPlaying }) {
                    Text(if (isPlaying) "暂停" else "播放")
                }
                Spacer(modifier = Modifier.width(12.dp))
                photos.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentIndex) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                            .clickable { currentIndex = index }
                    )
                }
            }
        }
    }
}

