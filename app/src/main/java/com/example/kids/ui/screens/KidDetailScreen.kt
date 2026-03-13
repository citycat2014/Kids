package com.example.kids.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kids.ui.components.DatePickerFieldWithClear
import com.example.kids.ui.kid.KidDetailUiState
import com.example.kids.ui.kid.KidDetailViewModel
import com.example.kids.ui.theme.AppleBackground
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@Composable
fun KidDetailScreen(
    kidId: Long?,
    onFinished: () -> Unit,
    onOpenGrowth: (Long) -> Unit,
    onOpenMood: (Long) -> Unit
) {
    val vm: KidDetailViewModel = viewModel()
    val state = vm.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 简单加载（幂等调用）:
    LaunchedEffect(kidId) {
        vm.load(kidId)
    }

    val context = LocalContext.current
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        vm.updateAvatar(uri?.toString())
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoUri?.let { vm.updateAvatar(it.toString()) }
        }
    }

    KidDetailContent(
        state = state.value,
        onNameChange = vm::updateName,
        onGenderChange = vm::updateGender,
        onBirthdayChange = vm::updateBirthday,
        onPickFromAlbum = { imagePickerLauncher.launch("image/*") },
        onTakePhoto = {
            val uri = createImageUri(context)
            pendingPhotoUri = uri
            cameraLauncher.launch(uri)
        },
        onSave = {
            coroutineScope.launch {
                vm.save()
                onFinished()
            }
        },
        onOpenGrowth = onOpenGrowth,
        onOpenMood = onOpenMood
    )
}

@Composable
private fun KidDetailContent(
    state: KidDetailUiState,
    onNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onBirthdayChange: (LocalDate?) -> Unit,
    onPickFromAlbum: () -> Unit,
    onTakePhoto: () -> Unit,
    onSave: () -> Unit,
    onOpenGrowth: (Long) -> Unit,
    onOpenMood: (Long) -> Unit
) {
    var showAvatarSourceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = if (state.isNew) "添加宝贝" else "编辑宝贝",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        KidAvatar(
            avatarUri = state.avatarUri,
            name = state.name,
            onClick = { showAvatarSourceDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("名字") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "性别",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenderChip(
                text = "男",
                selected = state.gender == "男",
                onClick = { onGenderChange("男") }
            )
            GenderChip(
                text = "女",
                selected = state.gender == "女",
                onClick = { onGenderChange("女") }
            )
            GenderChip(
                text = "保密",
                selected = state.gender.isBlank() || state.gender == "保密",
                onClick = { onGenderChange("保密") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DatePickerFieldWithClear(
            value = state.birthday,
            onValueChange = onBirthdayChange,
            label = { Text("生日（可选）") },
            placeholder = "点击选择生日"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "保存")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isNew && state.id != 0L) {
            Button(
                onClick = { onOpenGrowth(state.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "查看成长记录")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onOpenMood(state.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "查看乖不乖日历")
            }
        } else {
            Text(
                text = "保存后即可为宝贝记录身高体重和乖不乖日历",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (showAvatarSourceDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarSourceDialog = false },
                title = { Text(text = "设置头像") },
                text = {
                    Column {
                        Button(
                            onClick = {
                                showAvatarSourceDialog = false
                                onPickFromAlbum()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("从相册选择")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                showAvatarSourceDialog = false
                                onTakePhoto()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("拍照")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAvatarSourceDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}

@Composable
private fun GenderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        border = if (selected) null else null
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KidAvatar(
    avatarUri: String?,
    name: String,
    onClick: () -> Unit
) {
    val initial = name.firstOrNull()?.toString() ?: "宝"

    androidx.compose.material3.Surface(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        shape = CircleShape
    ) {
        if (avatarUri.isNullOrBlank()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "点击添加头像",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "宝贝头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile(
        "kid_avatar_${System.currentTimeMillis()}",
        ".jpg",
        imagesDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

