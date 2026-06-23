package com.example.hanger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.hanger.notifications.NotificationPollingService
import com.example.hanger.notifications.UnreadCountViewModel
import com.example.hanger.ui.theme.HangerTheme
import com.example.hanger.ui.screens.ExploreScreen
import com.example.hanger.ui.screens.NotificationsScreen
import com.hanger.app.data.model.User
import com.hanger.app.ui.auth.AuthViewModel
import com.hanger.app.ui.screens.CreatePostScreen
import com.hanger.app.ui.screens.FeedScreen
import com.hanger.app.ui.screens.LoginScreen
import com.hanger.app.ui.screens.PostDetailScreen
import com.hanger.app.ui.screens.ProfileScreen
import com.hanger.app.ui.screens.RegisterStep1Screen
import com.hanger.app.ui.screens.RegisterStep2Screen

private sealed class AppScreen {
    object Login : AppScreen()
    object RegisterStep1 : AppScreen()
    data class RegisterStep2(
        val firstName: String,
        val lastName: String,
        val username: String,
        val email: String,
        val password: String
    ) : AppScreen()
    object Feed : AppScreen()
    object Profile : AppScreen()
    object CreatePost : AppScreen()
    object Notifications : AppScreen()
    data class Explore(val initialQuery: String = "") : AppScreen()
    data class PostDetail(val postId: String) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val unreadCountVm: UnreadCountViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            HangerTheme(darkTheme = false) {
                var screen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
                var loggedInUser by remember { mutableStateOf<User?>(null) }
                val unreadCount by unreadCountVm.unreadCount.collectAsState()
                val hasNotifications = unreadCount > 0

                when (val s = screen) {
                    AppScreen.Login -> LoginScreen(
                        onLoginSuccess = { user ->
                            loggedInUser = user
                            startPollingService(user.id)
                            screen = AppScreen.Feed
                        },
                        onNavigateToRegister = { screen = AppScreen.RegisterStep1 },
                        viewModel = authVm
                    )

                    AppScreen.RegisterStep1 -> RegisterStep1Screen(
                        onNext = { firstName, lastName, username, email, password ->
                            screen = AppScreen.RegisterStep2(firstName, lastName, username, email, password)
                        },
                        onNavigateToLogin = { screen = AppScreen.Login },
                        viewModel = authVm
                    )

                    is AppScreen.RegisterStep2 -> RegisterStep2Screen(
                        firstName = s.firstName,
                        lastName = s.lastName,
                        username = s.username,
                        email = s.email,
                        password = s.password,
                        onRegisterSuccess = { user ->
                            loggedInUser = user
                            startPollingService(user.id)
                            screen = AppScreen.Feed
                        },
                        onBack = { screen = AppScreen.RegisterStep1 },
                        viewModel = authVm
                    )

                    AppScreen.Feed -> FeedScreen(
                        userInitials = loggedInUser?.username
                            ?.take(2)
                            ?.uppercase()
                            ?: "ME",
                        userId = loggedInUser?.id ?: "",
                        hasNotifications = hasNotifications,
                        onNavigateToProfile = { screen = AppScreen.Profile },
                        onNavigateToExplore = { screen = AppScreen.Explore() },
                        onNavigateToCamera = { screen = AppScreen.CreatePost },
                        onNavigateToNotifications = { screen = AppScreen.Notifications },
                        onNavigateToPost = { postId -> screen = AppScreen.PostDetail(postId) }
                    )

                    AppScreen.CreatePost -> CreatePostScreen(
                        userId = loggedInUser?.id ?: "",
                        onNavigateBack = { screen = AppScreen.Feed },
                        onPostCreated = { screen = AppScreen.Feed }
                    )

                    AppScreen.Profile -> {
                        val user = loggedInUser
                        if (user != null) {
                            ProfileScreen(
                                user = user,
                                hasNotifications = hasNotifications,
                                onNavigateBack = { screen = AppScreen.Feed },
                                onNavigateToFeed = { screen = AppScreen.Feed },
                                onNavigateToNotifications = { screen = AppScreen.Notifications },
                                onLogout = {
                                    authVm.logout()
                                    loggedInUser = null
                                    screen = AppScreen.Login
                                }
                            )
                        }
                    }

                    AppScreen.Notifications -> NotificationsScreen(
                        userId = loggedInUser?.id ?: "",
                        hasNotifications = hasNotifications,
                        onAllRead = { unreadCountVm.markAllRead() },
                        onNavigateToFeed = { screen = AppScreen.Feed },
                        onNavigateToExplore = { screen = AppScreen.Explore() },
                        onNavigateToCamera = { screen = AppScreen.CreatePost },
                        onNavigateToProfile = { screen = AppScreen.Profile },
                        onNavigateToPost = { postId -> screen = AppScreen.PostDetail(postId) }
                    )

                    is AppScreen.Explore -> ExploreScreen(
                        userId = loggedInUser?.id ?: "",
                        userInitials = loggedInUser?.username?.take(2)?.uppercase() ?: "ME",
                        initialQuery = s.initialQuery,
                        hasNotifications = hasNotifications,
                        onNavigateToFeed = { screen = AppScreen.Feed },
                        onNavigateToProfile = { screen = AppScreen.Profile },
                        onNavigateToNotifications = { screen = AppScreen.Notifications },
                        onNavigateToPost = { postId -> screen = AppScreen.PostDetail(postId) }
                    )

                    is AppScreen.PostDetail -> PostDetailScreen(
                        postId = s.postId,
                        userId = loggedInUser?.id ?: "",
                        onNavigateBack = { screen = AppScreen.Feed }
                    )
                }
            }
        }
    }

    private fun startPollingService(userId: String) {
        val intent = NotificationPollingService.buildIntent(this, userId)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onStart() {
        super.onStart()
        unreadCountVm.register(this)
    }

    override fun onStop() {
        super.onStop()
        unreadCountVm.unregister(this)
    }
}
