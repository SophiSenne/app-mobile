package com.example.hanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hanger.ui.theme.HangerTheme
import com.hanger.app.ui.auth.LoginScreen
import com.hanger.app.ui.auth.RegisterScreen

private enum class AuthScreen {
    LOGIN, REGISTER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangerTheme(darkTheme = true) {
                var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

                when (currentScreen) {
                    AuthScreen.LOGIN -> LoginScreen(
                        onLoginSuccess = {},
                        onNavigateToRegister = { currentScreen = AuthScreen.REGISTER }
                    )

                    AuthScreen.REGISTER -> RegisterScreen(
                        onRegisterSuccess = {},
                        onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
                    )
                }
            }
        }
    }
}