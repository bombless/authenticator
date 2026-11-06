package com.bombless.authenticator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.bombless.authenticator.ui.AuthenticatorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFB8C7FF), background = Color(0xFF101318),
                surface = Color(0xFF191E25), onSurface = Color(0xFFE2E6EF)
            )) {
                AuthenticatorScreen()
            }
        }
    }
}
