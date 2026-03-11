package com.example.kids.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kids.ui.growth.GrowthRecordUi
import com.example.kids.ui.growth.GrowthRecordViewModel
import com.example.kids.ui.theme.AppleBackground
import com.example.kids.ui.utils.collectAsStateWithLifecycleSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Helper function to get current location
private fun getCurrentLocation(context: Context, onLocationResult: (Double?, Double?) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

        // Check if GPS is enabled
        val gpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            onLocationResult(null, null)
            return
        }

        // Get last known location (faster)
        var bestLocation: android.location.Location? = null
        val minTime = 2000L // 2 seconds
        val minDistance = 10f // 10 meters

        if (gpsEnabled) {
            val gpsLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            if (gpsLocation != null && gpsLocation.time > System.currentTimeMillis() - minTime) {
                bestLocation = gpsLocation
            }
        }

        if (networkEnabled && bestLocation == null) {
            val networkLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            if (networkLocation != null && networkLocation.time > System.currentTimeMillis() - minTime) {
                bestLocation = networkLocation
            }
        }

        if (bestLocation != null) {
            onLocationResult(bestLocation.latitude, bestLocation.longitude)
        } else {
            onLocationResult(null, null)
        }
    } catch (e: Exception) {
        onLocationResult(null, null)
    }
}

@Composable
fun GrowthRecordScreen(
    kidId: Long,
    onBack: () -> Unit,
    onOpenTimeline: (Long) -> Unit
) {
    val vm: GrowthRecordViewModel = viewModel()
    val state by vm.uiState
        .collectAsStateWithLifecycleSafe()
    val context = LocalContext.current

    LaunchedEffect(kidId) {
        vm.load(kidId)
    }

    GrowthRecordContent(
        records = state.records,
        context = context,
        onBack = onBack,
        onOpenTimeline = { onOpenTimeline(kidId) },
        onSaveRecord = { record ->
            vm.addOrUpdate(
                id = record.id,
                kidId = state.kidId,
                date = record.date,
                heightCm = record.heightCm,
                weightKg = record.weightKg,
                note = record.note,
                photoUri = record.photoUri
            )
        },
        onDelete = { vm.delete(it) }
    )
}

@Composable
private fun GrowthRecordContent(
    records: List<GrowthRecordUi>,
    context: Context,
    onBack: () -> Unit,
    onOpenTimeline: () -> Unit,
    onSaveRecord: (GrowthRecordUi) -> Unit,
    onDelete: (Long) -> Unit
) {
    var editing by remember { mutableStateOf<GrowthRecordUi?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var pendingRecord by remember { mutableStateOf<GrowthRecordUi?>(null) }
    var duplicateTarget by remember { mutableStateOf<GrowthRecordUi?>(null) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = "成长记录",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "记录身高体重和成长照片",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onOpenTimeline) {
                        Text("成长影像")
                    }
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    isDialogOpen = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
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
            GrowthChart(records = records)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { item ->
                    GrowthRecordRow(
                        record = item,
                        onEdit = {
                            editing = it
                            isDialogOpen = true
                        },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }

    if (isDialogOpen) {
        GrowthRecordDialog(
            initial = editing,
            context = context,
            onDismiss = { isDialogOpen = false },
            onConfirm = { updated ->
                val existingSameDate = records.firstOrNull {
                    it.date == updated.date && it.id != updated.id && it.id != 0L
                }
                if (existingSameDate != null && updated.id == 0L) {
                    pendingRecord = updated
                    duplicateTarget = existingSameDate
                    showDuplicateDialog = true
                } else {
                    onSaveRecord(updated)
                    isDialogOpen = false
                }
            }
        )
    }

    if (showDuplicateDialog && pendingRecord != null && duplicateTarget != null) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text(text = "同一天已有记录") },
            text = {
                Text(
                    text = "该日期已经有一条成长记录，是否用新的数据覆盖原记录？",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val base = pendingRecord!!
                        val target = duplicateTarget!!
                        onSaveRecord(
                            base.copy(id = target.id)
                        )
                        showDuplicateDialog = false
                        isDialogOpen = false
                        pendingRecord = null
                        duplicateTarget = null
                    }
                ) {
                    Text("覆盖更新")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDuplicateDialog = false
                        // 保留编辑弹窗，用户可以修改日期或内容
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun GrowthRecordRow(
    record: GrowthRecordUi,
    onEdit: (GrowthRecordUi) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = record.date.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = buildString {
                    if (record.heightCm != null) append("身高 ${record.heightCm} cm  ")
                    if (record.weightKg != null) append("体重 ${record.weightKg} kg")
                    if (isEmpty()) append("未填写身高体重")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            if (!record.note.isNullOrBlank()) {
                Text(
                    text = record.note ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            if (!record.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(record.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "记录照片",
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Row {
            IconButton(onClick = { onEdit(record) }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

@Composable
private fun GrowthRecordDialog(
    initial: GrowthRecordUi?,
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (GrowthRecordUi) -> Unit
) {
    var dateText by remember {
        mutableStateOf(
            (initial?.date ?: LocalDate.now()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }
    var heightText by remember { mutableStateOf(initial?.heightCm?.toString().orEmpty()) }
    var weightText by remember { mutableStateOf(initial?.weightKg?.toString().orEmpty()) }
    var noteText by remember { mutableStateOf(initial?.note.orEmpty()) }
    var photoUri by remember { mutableStateOf(initial?.photoUri) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Location related states
    var currentLatitude by remember { mutableStateOf<Double?>(initial?.latitude) }
    var currentLongitude by remember { mutableStateOf<Double?>(initial?.longitude) }
    var latText by remember { mutableStateOf(initial?.latitude?.toString().orEmpty()) }
    var lngText by remember { mutableStateOf(initial?.longitude?.toString().orEmpty()) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            getCurrentLocation(context) { lat, lng ->
                currentLatitude = lat
                currentLongitude = lng
                latText = lat?.toString().orEmpty()
                lngText = lng?.toString().orEmpty()
                locationError = null
            }
        } else {
            locationError = "需要位置权限才能获取当前位置"
        }
    }

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri?.toString()
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoUri?.let { photoUri = it.toString() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (initial == null) "添加记录" else "编辑记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("日期 (YYYY-MM-DD)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("身高 (cm，可选)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("体重 (kg，可选)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("备注（可选）") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") }
                    ) {
                        Text("相册照片")
                    }
                    OutlinedButton(
                        onClick = {
                            val uri = createGrowthImageUri(context)
                            pendingPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    ) {
                        Text("拍照")
                    }
                }
                if (!photoUri.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "记录照片预览",
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Location section
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "位置信息",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Display current coordinates if available
                if (currentLatitude != null && currentLongitude != null) {
                    Text(
                        text = "当前位置：${String.format("%.6f", currentLatitude!!)}, ${String.format("%.6f", currentLongitude!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Buttons for location
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("获取当前位置")
                    }
                    OutlinedButton(
                        onClick = {
                            if (latText.isNotBlank() && lngText.isNotBlank()) {
                                currentLatitude = latText.toDoubleOrNull()
                                currentLongitude = lngText.toDoubleOrNull()
                                locationError = if (currentLatitude == null || currentLongitude == null) {
                                    "坐标格式不正确"
                                } else {
                                    null
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("手动输入")
                    }
                }

                // Manual coordinate input
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("纬度 (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("经度 (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Clear location button
                if (currentLatitude != null || currentLongitude != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            currentLatitude = null
                            currentLongitude = null
                            latText = ""
                            lngText = ""
                            locationError = null
                        }
                    ) {
                        Text("清除位置")
                    }
                }

                if (locationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = locationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val date = try {
                        LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE)
                    } catch (e: DateTimeParseException) {
                        errorText = "日期格式不正确"
                        return@TextButton
                    }

                    val height = heightText.toFloatOrNull()
                    val weight = weightText.toFloatOrNull()
                    val note = noteText.ifBlank { null }

                    onConfirm(
                        GrowthRecordUi(
                            id = initial?.id ?: 0L,
                            date = date,
                            heightCm = height,
                            weightKg = weight,
                            note = note,
                            photoUri = photoUri,
                            latitude = currentLatitude,
                            longitude = currentLongitude
                        )
                    )
                }
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

private enum class GrowthChartMode { BOTH, HEIGHT_ONLY, WEIGHT_ONLY }

@Composable
private fun GrowthChart(records: List<GrowthRecordUi>) {
    if (records.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "添加身高体重记录后，这里会显示成长曲线",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    var mode by remember { mutableStateOf(GrowthChartMode.BOTH) }

    val heightValues = records.mapNotNull { it.heightCm }
    val weightValues = records.mapNotNull { it.weightKg }

    val activeValues = when (mode) {
        GrowthChartMode.BOTH -> heightValues + weightValues
        GrowthChartMode.HEIGHT_ONLY -> heightValues
        GrowthChartMode.WEIGHT_ONLY -> weightValues
    }

    val maxValue = activeValues
        .takeIf { it.isNotEmpty() }
        ?.maxOrNull() ?: 0f
    val minValue = activeValues
        .takeIf { it.isNotEmpty() }
        ?.minOrNull() ?: 0f

    val valueRange = (maxValue - minValue).takeIf { it > 0f } ?: 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Text(
            text = "成长曲线",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipButton(
                text = "身高+体重",
                selected = mode == GrowthChartMode.BOTH,
                onClick = { mode = GrowthChartMode.BOTH }
            )
            FilterChipButton(
                text = "仅身高",
                selected = mode == GrowthChartMode.HEIGHT_ONLY,
                onClick = { mode = GrowthChartMode.HEIGHT_ONLY }
            )
            FilterChipButton(
                text = "仅体重",
                selected = mode == GrowthChartMode.WEIGHT_ONLY,
                onClick = { mode = GrowthChartMode.WEIGHT_ONLY }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (records.size == 1) {
                // 单个点时，画圆点即可
                val x = size.width / 2f
                val heightValue = records.first().heightCm
                val weightValue = records.first().weightKg

                if (heightValue != null) {
                    val y = size.height - (heightValue - minValue) / valueRange * size.height
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
                if (weightValue != null) {
                    val y = size.height - (weightValue - minValue) / valueRange * size.height
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            } else {
                val stepX = if (records.size <= 1) 0f else size.width / (records.size - 1).coerceAtLeast(1)

                fun valueToY(v: Float): Float =
                    size.height - (v - minValue) / valueRange * size.height

                // 背景网格线
                val gridLines = 4
                val stepY = size.height / gridLines
                repeat(gridLines + 1) { i ->
                    val y = stepY * i
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 身高曲线
                if (mode == GrowthChartMode.BOTH || mode == GrowthChartMode.HEIGHT_ONLY) {
                    var lastPoint: Offset? = null
                    records.forEachIndexed { index, record ->
                        val value = record.heightCm ?: return@forEachIndexed
                        val x = stepX * index
                        val y = valueToY(value)
                        val current = Offset(x, y)
                        if (lastPoint != null) {
                            drawLine(
                                color = Color(0xFF4CAF50),
                                start = lastPoint!!,
                                end = current,
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        lastPoint = current
                    }
                }

                // 体重曲线
                if (mode == GrowthChartMode.BOTH || mode == GrowthChartMode.WEIGHT_ONLY) {
                    var lastPoint: Offset? = null
                    records.forEachIndexed { index, record ->
                        val value = record.weightKg ?: return@forEachIndexed
                        val x = stepX * index
                        val y = valueToY(value)
                        val current = Offset(x, y)
                        if (lastPoint != null) {
                            drawLine(
                                color = Color(0xFF2196F3),
                                start = lastPoint!!,
                                end = current,
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        lastPoint = current
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendDot(color = Color(0xFF4CAF50), label = "身高")
            LegendDot(color = Color(0xFF2196F3), label = "体重")
        }
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        FilledTonalButton(onClick = onClick) {
            Text(text)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .height(10.dp)
                .fillMaxWidth(fraction = 0.05f)
        ) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2f,
                center = center
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun <T> StateFlow<T>.collectAsStateWithLifecycleSafe(): androidx.compose.runtime.State<T> {
    // 简单封装，避免在此样例中引入 lifecycle-compose 依赖
    val flow = this
    val state = remember { mutableStateOf(flow.value) }
    LaunchedEffect(flow) {
        withContext(Dispatchers.Main.immediate) {
            flow.collect { state.value = it }
        }
    }
    return state
}

private fun createGrowthImageUri(context: Context): Uri {
    val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile(
        "growth_photo_${System.currentTimeMillis()}",
        ".jpg",
        imagesDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

