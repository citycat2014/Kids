package com.example.kids.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kids.data.model.GrowthStandard
import com.example.kids.ui.theme.AppleBackground

@Composable
fun GrowthStandardScreen(
    kidGender: String,
    currentAge: Int?,
    onBack: () -> Unit
) {
    var selectedGender by remember { mutableStateOf(kidGender) }
    val isBoy = selectedGender == "男"

    // Get all standards for the selected gender
    val heightStandards = remember(selectedGender) {
        GrowthStandard.getAllHeightStandards(selectedGender)
    }
    val weightStandards = remember(selectedGender) {
        GrowthStandard.getAllWeightStandards(selectedGender)
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "成长标准表",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "1-18岁${if (isBoy) "男孩" else "女孩"}身高体重标准",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
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
            // Gender selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GenderChip(
                    text = "男孩",
                    selected = isBoy,
                    onClick = { selectedGender = "男" }
                )
                GenderChip(
                    text = "女孩",
                    selected = !isBoy,
                    onClick = { selectedGender = "女" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Table header
            StandardTableHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Table content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items((1..18).toList()) { age ->
                    val isCurrentAge = age == currentAge
                    val heightValues = heightStandards[age]
                    val weightValues = weightStandards[age]

                    StandardTableRow(
                        age = age,
                        heightRange = heightValues?.let {
                            "${it[0]}-${it[3]}"
                        } ?: "-",
                        weightRange = weightValues?.let {
                            "${it[0]}-${it[3]}"
                        } ?: "-",
                        isHighlighted = isCurrentAge
                    )
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(16.dp))
            LegendSection()
        }
    }
}

@Composable
private fun GenderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StandardTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "年龄",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "身高范围 (cm)",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "体重范围 (kg)",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StandardTableRow(
    age: Int,
    heightRange: String,
    weightRange: String,
    isHighlighted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHighlighted -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                age % 2 == 0 -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${age}岁",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Center,
                color = if (isHighlighted) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = heightRange,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center,
                color = if (isHighlighted) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = weightRange,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center,
                color = if (isHighlighted) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LegendSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "说明",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Height legend
        Text(
            text = "身高标准 (cm)：",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        LegendItem(color = Color(0xFFE57373), text = "矮小 / 超高")
        LegendItem(color = Color(0xFFFFB74D), text = "偏矮")
        LegendItem(color = Color(0xFF81C784), text = "标准范围")

        Spacer(modifier = Modifier.height(8.dp))

        // Weight legend
        Text(
            text = "体重标准 (kg)：",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        LegendItem(color = Color(0xFFE57373), text = "肥胖")
        LegendItem(color = Color(0xFFFFB74D), text = "偏瘦 / 超重")
        LegendItem(color = Color(0xFF81C784), text = "标准范围")

        Spacer(modifier = Modifier.height(8.dp))

        // Note about highlighted row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "高亮行 = 当前年龄段",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
