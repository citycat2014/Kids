package com.example.kids.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ApplePrimary,
    secondary = AppleSecondary,
    background = AppleBackground,
    surface = AppleCard,
    onPrimary = AppleCard,
    onSecondary = AppleCard,
    onBackground = androidx.compose.ui.graphics.Color(0xFF111111),
    onSurface = androidx.compose.ui.graphics.Color(0xFF111111)
)

@Composable
fun KidsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

