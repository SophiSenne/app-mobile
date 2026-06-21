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
import com.hanger.app.data.model.User
import com.hanger.app.ui.screens.FeedScreen
import com.hanger.app.ui.screens.LoginScreen
import com.hanger.app.ui.screens.ProfileScreen
import com.hanger.app.ui.screens.RegisterScreen

private enum class AppScreen { LOGIN, REGISTER, FEED, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangerTheme(darkTheme = false) {
                var screen by remember { mutableStateOf(AppScreen.LOGIN) }
                var loggedInUser by remember { mutableStateOf<User?>(null) }

                when (screen) {
                    AppScreen.LOGIN -> LoginScreen(
                        onLoginSuccess = { user ->
                            loggedInUser = user
                            screen = AppScreen.FEED
                        },
                        onNavigateToRegister = { screen = AppScreen.REGISTER }
                    )

                    AppScreen.REGISTER -> RegisterScreen(
                        onRegisterSuccess = { user ->
                            loggedInUser = user
                            screen = AppScreen.FEED
                        },
                        onNavigateToLogin = { screen = AppScreen.LOGIN }
                    )

                    AppScreen.FEED -> FeedScreen(
                        userInitials = loggedInUser?.username
                            ?.take(2)
                            ?.uppercase()
                            ?: "ME",
                        userId = loggedInUser?.id ?: "",
                        onNavigateToProfile = { screen = AppScreen.PROFILE }
                    )

                    AppScreen.PROFILE -> {
                        val user = loggedInUser
                        if (user != null) {
                            ProfileScreen(
                                user = user,
                                onNavigateBack = { screen = AppScreen.FEED },
                                onNavigateToFeed = { screen = AppScreen.FEED }
                            )
                        }
                    }
                }
            }
        }
    }
}
