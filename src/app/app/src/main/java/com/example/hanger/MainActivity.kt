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
import com.hanger.app.ui.screens.PostDetailScreen
import com.hanger.app.ui.screens.ProfileScreen
import com.hanger.app.ui.screens.RegisterScreen

private sealed class AppScreen {
    object Login : AppScreen()
    object Register : AppScreen()
    object Feed : AppScreen()
    object Profile : AppScreen()
    data class PostDetail(val postId: String) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangerTheme(darkTheme = false) {
                var screen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
                var loggedInUser by remember { mutableStateOf<User?>(null) }

                when (val s = screen) {
                    AppScreen.Login -> LoginScreen(
                        onLoginSuccess = { user ->
                            loggedInUser = user
                            screen = AppScreen.Feed
                        },
                        onNavigateToRegister = { screen = AppScreen.Register }
                    )

                    AppScreen.Register -> RegisterScreen(
                        onRegisterSuccess = { user ->
                            loggedInUser = user
                            screen = AppScreen.Feed
                        },
                        onNavigateToLogin = { screen = AppScreen.Login }
                    )

                    AppScreen.Feed -> FeedScreen(
                        userInitials = loggedInUser?.username
                            ?.take(2)
                            ?.uppercase()
                            ?: "ME",
                        userId = loggedInUser?.id ?: "",
                        onNavigateToProfile = { screen = AppScreen.Profile },
                        onNavigateToPost = { postId -> screen = AppScreen.PostDetail(postId) }
                    )

                    AppScreen.Profile -> {
                        val user = loggedInUser
                        if (user != null) {
                            ProfileScreen(
                                user = user,
                                onNavigateBack = { screen = AppScreen.Feed },
                                onNavigateToFeed = { screen = AppScreen.Feed }
                            )
                        }
                    }

                    is AppScreen.PostDetail -> PostDetailScreen(
                        postId = s.postId,
                        userId = loggedInUser?.id ?: "",
                        onNavigateBack = { screen = AppScreen.Feed }
                    )
                }
            }
        }
    }
}
