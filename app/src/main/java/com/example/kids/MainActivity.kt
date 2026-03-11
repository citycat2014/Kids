package com.example.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.kids.ui.KidsNavHost
import com.example.kids.ui.theme.KidsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            KidsAppRoot()
        }
    }
}

@Composable
private fun KidsAppRoot() {
    val darkTheme = isSystemInDarkTheme()

    KidsTheme(darkTheme = darkTheme) {
        Surface(color = Color.Transparent) {
            KidsNavHost()
        }
    }
}

