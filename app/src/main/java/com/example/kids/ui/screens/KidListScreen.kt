package com.example.kids.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kids.ui.mood.KidMood
import com.example.kids.ui.theme.AppleBackground
import com.example.kids.ui.theme.AppleCard
import com.example.kids.ui.utils.getLunarDate
import com.example.kids.ui.utils.getWeekDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class KidListItemUi(
    val id: Long,
    val name: String,
    val subtitle: String,
    val avatarUri: String?,
    val gradeLevel: String? = null,  // 用于判断显示哪些入口
    val todaySummary: TodaySummary? = null
)

data class TodaySummary(
    val mood: KidMood? = null,
    val totalExerciseMinutes: Int = 0
)

@Composable
fun KidListScreen(
    kids: List<KidListItemUi> = emptyList(),
    onAddKid: () -> Unit,
    onViewGrowth: (Long) -> Unit,
    onViewMood: (Long) -> Unit,
    onEditKid: (Long) -> Unit,
    onViewAcademic: (Long, String?) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 日期展示区域与标题同行
        val today = remember { LocalDate.now() }
        val lunarDate = remember(today) { getLunarDate(today) }
        val weekDay = remember(today) { getWeekDay(today) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "宝贝",
                style = MaterialTheme.typography.headlineLarge
            )

            // 日期展示移至右侧
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("M月d日")) + " $weekDay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Text(
                    text = lunarDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "选择一个宝贝开始记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddKid) {
            Text(text = "添加宝贝")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(kids, key = { it.id }) { kid ->
                KidCard(
                    item = kid,
                    onViewGrowth = { onViewGrowth(kid.id) },
                    onViewMood = { onViewMood(kid.id) },
                    onEdit = { onEditKid(kid.id) },
                    onViewAcademic = { onViewAcademic(kid.id, kid.gradeLevel) }
                )
            }
        }
    }
}

@Composable
private fun KidCard(
    item: KidListItemUi,
    onViewGrowth: () -> Unit,
    onViewMood: () -> Unit,
    onEdit: () -> Unit,
    onViewAcademic: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape),
        colors = CardDefaults.cardColors(
            containerColor = AppleCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                KidAvatarThumbnail(
                    avatarUri = item.avatarUri,
                    name = item.name
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑资料",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    // Today summary chips
                    item.todaySummary?.let { summary ->
                        if (summary.mood != null || summary.totalExerciseMinutes > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                summary.mood?.let { mood ->
                                    val (moodText, moodColor) = when (mood) {
                                        KidMood.GOOD -> "乖" to androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                        KidMood.OK -> "一般" to androidx.compose.ui.graphics.Color(0xFFFFC107)
                                        KidMood.BAD -> "不乖" to androidx.compose.ui.graphics.Color(0xFFF44336)
                                    }
                                    androidx.compose.material3.Surface(
                                        color = moodColor.copy(alpha = 0.15f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = moodText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = moodColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (summary.totalExerciseMinutes > 0) {
                                    androidx.compose.material3.Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "运动${summary.totalExerciseMinutes}分钟",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 顺序：乖不乖日历 → 学习 → 成长
                OutlinedButton(
                    onClick = onViewMood,
                    contentPadding = androidx.compose.material3.ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text("乖不乖日历")
                }
                OutlinedButton(
                    onClick = onViewAcademic,
                    contentPadding = androidx.compose.material3.ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text("学习")
                }
                OutlinedButton(
                    onClick = onViewGrowth,
                    contentPadding = androidx.compose.material3.ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text("成长")
                }
            }
        }
    }
}

@Composable
private fun KidAvatarThumbnail(
    avatarUri: String?,
    name: String
) {
    val initial = name.firstOrNull()?.toString() ?: "宝"

    // 根据名字生成一致的柔和背景色
    val backgroundColor = remember(name) {
        val colors = listOf(
            androidx.compose.ui.graphics.Color(0xFFE3F2FD), // 蓝色系
            androidx.compose.ui.graphics.Color(0xFFFFEBEE), // 粉色系
            androidx.compose.ui.graphics.Color(0xFFE8F5E9), // 绿色系
            androidx.compose.ui.graphics.Color(0xFFEDE7F6), // 紫色系
            androidx.compose.ui.graphics.Color(0xFFF6E5C4), // 橙色系
            androidx.compose.ui.graphics.Color(0xFFE0F2F1)  // 青色系
        )
        colors[name.lowercase().firstOrNull()?.code?.rem(colors.size.toInt()) ?: 0]
    }

    androidx.compose.material3.Surface(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape),
        color = backgroundColor,
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

