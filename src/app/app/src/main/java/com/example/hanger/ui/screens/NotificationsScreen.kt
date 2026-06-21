package com.example.hanger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hanger.ui.theme.HangerBeige
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerSurfaceAlt
import com.example.hanger.ui.theme.HangerTextMuted
import com.example.hanger.ui.theme.HangerTextSecondary
import com.example.hanger.viewmodels.NotificationsViewModel
import com.hanger.app.data.model.NotificationDto
import com.hanger.app.data.model.NotificationType
import com.hanger.app.ui.feed.components.FeedBottomNav
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    userId: String,
    hasNotifications: Boolean = false,
    onAllRead: () -> Unit = {},
    onNavigateToFeed: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {}
) {
    val factory = remember(userId) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NotificationsViewModel(userId) as T
        }
    }
    val vm: NotificationsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = HangerCream,
        bottomBar = {
            FeedBottomNav(
                currentRoute = "notifications",
                hasNotifications = hasNotifications,
                onHomeClick = onNavigateToFeed,
                onExploreClick = onNavigateToExplore,
                onCameraClick = onNavigateToCamera,
                onNotificationsClick = {},
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ─── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Atividade",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HangerInk,
                    modifier = Modifier.weight(1f)
                )
                if (state.unreadCount > 0) {
                    IconButton(onClick = { vm.markAllRead(); onAllRead() }) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Marcar todas como lidas",
                            tint = HangerPink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)

            // ─── Content ─────────────────────────────────────────────────────
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HangerPink)
                    }
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = vm::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.notifications.isEmpty() && !state.isRefreshing) {
                            EmptyNotifications()
                        } else {
                            NotificationsList(
                                notifications = state.notifications,
                                onNotificationClick = { notification ->
                                    notification.postId?.let { onNavigateToPost(it) }
                                }
                            )
                        }
                    }
                }
            }

            // ─── Error toast ─────────────────────────────────────────────────
            state.errorMessage?.let { error ->
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(4_000)
                    vm.dismissError()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = HangerPink,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ─── Notifications list ───────────────────────────────────────────────────────

@Composable
private fun NotificationsList(
    notifications: List<NotificationDto>,
    onNotificationClick: (NotificationDto) -> Unit
) {
    // Group by date section: "Hoje", "Esta semana", "Anteriores"
    val now = Instant.now()
    val todayStart = now.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
    val weekStart = todayStart.minus(6, ChronoUnit.DAYS)

    val today = notifications.filter {
        runCatching { Instant.parse(it.createdAt) >= todayStart }.getOrDefault(false)
    }
    val thisWeek = notifications.filter {
        runCatching {
            val t = Instant.parse(it.createdAt)
            t < todayStart && t >= weekStart
        }.getOrDefault(false)
    }
    val older = notifications.filter {
        runCatching { Instant.parse(it.createdAt) < weekStart }.getOrDefault(false)
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (today.isNotEmpty()) {
            item { SectionHeader("Hoje") }
            items(items = today, key = { it.id }) { n ->
                NotificationRow(notification = n, onClick = { onNotificationClick(n) })
                HorizontalDivider(color = Color(0x0D000000), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        if (thisWeek.isNotEmpty()) {
            item { SectionHeader("Esta semana") }
            items(items = thisWeek, key = { it.id }) { n ->
                NotificationRow(notification = n, onClick = { onNotificationClick(n) })
                HorizontalDivider(color = Color(0x0D000000), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        if (older.isNotEmpty()) {
            item { SectionHeader("Anteriores") }
            items(items = older, key = { it.id }) { n ->
                NotificationRow(notification = n, onClick = { onNotificationClick(n) })
                HorizontalDivider(color = Color(0x0D000000), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = HangerTextMuted,
        letterSpacing = 0.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun NotificationRow(
    notification: NotificationDto,
    onClick: () -> Unit
) {
    val type = NotificationType.from(notification.type)
    val isUnread = !notification.read

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) HangerBeige else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(44.dp)) {
            if (notification.actorAvatarUrl != null) {
                AsyncImage(
                    model = notification.actorAvatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(HangerSurfaceAlt),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notification.actorUsername.take(2).uppercase(),
                        color = HangerInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Tipo badge no canto
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(notificationColor(type))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notificationIcon(type),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Texto + miniatura do post
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildNotificationText(notification, type),
                fontSize = 13.sp,
                color = HangerInk,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = relativeTime(notification.createdAt),
                fontSize = 11.sp,
                color = HangerTextMuted
            )
        }

        // Miniatura do post se houver
        if (notification.postImageUrl != null) {
            Spacer(modifier = Modifier.width(12.dp))
            AsyncImage(
                model = notification.postImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        // Indicador de não lido
        if (isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(HangerPink)
            )
        }
    }
}

private fun buildNotificationText(n: NotificationDto, type: NotificationType): String =
    when (type) {
        NotificationType.LIKE -> "@${n.actorUsername} curtiu sua publicação"
        NotificationType.COMMENT -> {
            val preview = n.commentContent?.take(60)?.let { "\"$it\"" } ?: ""
            "@${n.actorUsername} comentou na sua publicação $preview".trim()
        }
        NotificationType.FOLLOW -> "@${n.actorUsername} começou a seguir você"
        NotificationType.UNKNOWN -> "@${n.actorUsername} interagiu com você"
    }

private fun notificationIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.LIKE -> Icons.Filled.FavoriteBorder
    NotificationType.COMMENT -> Icons.Filled.ModeComment
    NotificationType.FOLLOW -> Icons.Filled.PersonAdd
    NotificationType.UNKNOWN -> Icons.Filled.Notifications
}

private fun notificationColor(type: NotificationType): Color = when (type) {
    NotificationType.LIKE -> Color(0xFFD63F72)
    NotificationType.COMMENT -> Color(0xFF5B8DEF)
    NotificationType.FOLLOW -> Color(0xFF5C3D52)
    NotificationType.UNKNOWN -> Color(0xFFAAAAAA)
}

private fun relativeTime(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        when {
            minutes < 1 -> "agora"
            minutes < 60 -> "${minutes}min"
            minutes < 1440 -> "${minutes / 60}h"
            minutes < 10080 -> "${minutes / 1440}d"
            else -> DateTimeFormatter.ofPattern("d 'de' MMM", Locale.forLanguageTag("pt-BR"))
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }
    } catch (e: Exception) {
        ""
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyNotifications() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = HangerTextMuted,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhuma atividade ainda",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Quando alguém curtir ou comentar nos seus looks, você verá aqui",
                fontSize = 13.sp,
                color = HangerTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
