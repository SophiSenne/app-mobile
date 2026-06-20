package com.hanger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.model.User
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.ui.feed.components.FeedBottomNav
import com.hanger.app.ui.profile.ProfileViewModel
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerGold
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerPlaceholderGradients
import com.example.hanger.ui.theme.HangerPlum
import com.example.hanger.ui.theme.HangerTextMuted

private enum class ProfileTab(val icon: ImageVector, val label: String) {
    POSTS(Icons.Filled.GridView, "Looks"),
    SAVED(Icons.Filled.Bookmark, "Salvos"),
    TAGGED(Icons.Filled.LocalOffer, "Marcados")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onNavigateBack: () -> Unit = {},
    onNavigateToFeed: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val factory = remember(user.id) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(PostsRepository(), user.id) as T
        }
    }
    val viewModel: ProfileViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }

    val userInitials = user.username.take(2).uppercase()

    Scaffold(
        containerColor = HangerCream,
        bottomBar = {
            FeedBottomNav(
                currentRoute = "profile",
                onHomeClick = onNavigateToFeed,
                onExploreClick = onNavigateToExplore,
                onCameraClick = onNavigateToCamera,
                onNotificationsClick = onNavigateToNotifications,
                onProfileClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ─── Top bar ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = HangerInk
                    )
                }
                Text(
                    text = "Meu Perfil",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HangerInk,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp)
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = HangerInk
                    )
                }
            }

            HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)

            // ─── Grid: header + tabs + posts ──────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Profile header (full width)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProfileHeader(
                        user = user,
                        userInitials = userInitials,
                        postsCount = state.posts.size
                    )
                }

                // Tab row (full width)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProfileTabRow(
                        selected = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                // Grid content
                when (selectedTab) {
                    ProfileTab.POSTS -> {
                        if (state.isLoading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = HangerPink, strokeWidth = 2.dp)
                                }
                            }
                        } else if (state.posts.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ProfileEmptyState(
                                    emoji = "🪡",
                                    message = "Nenhum look postado ainda",
                                    sub = "Que tal compartilhar seu primeiro look?"
                                )
                            }
                        } else {
                            items(state.posts, key = { it.id }) { post ->
                                ProfileGridItem(post = post)
                            }
                        }
                    }

                    ProfileTab.SAVED -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ProfileEmptyState(
                                emoji = "🔖",
                                message = "Nenhum look salvo ainda",
                                sub = "Salve looks que te inspiram!"
                            )
                        }
                    }

                    ProfileTab.TAGGED -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ProfileEmptyState(
                                emoji = "🏷️",
                                message = "Nenhuma marcação ainda",
                                sub = "Looks em que você aparece aparecem aqui"
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Profile Header ────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    user: User,
    userInitials: String,
    postsCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HangerCream)
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        // Avatar + stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(HangerPlum),
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = userInitials,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HangerGold
                    )
                }
            }

            // Stats
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    number = if (postsCount > 0) postsCount.toString() else "—",
                    label = "looks"
                )
                ProfileStat(number = "—", label = "seguidores")
                ProfileStat(number = "—", label = "seguindo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Name
        Text(
            text = user.username,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = HangerInk
        )

        // Bio + tagline
        val bioText = user.bio
        if (!bioText.isNullOrBlank()) {
            Text(
                text = bioText,
                fontSize = 13.sp,
                color = Color(0xFF555555),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // City · VISTA SUA HISTÓRIA
        val cityLine = buildAnnotatedString {
            if (!user.locationCity.isNullOrBlank()) {
                withStyle(SpanStyle(fontSize = 12.sp, color = Color(0xFF555555))) {
                    append(user.locationCity)
                    append(" · ")
                }
            }
            withStyle(
                SpanStyle(
                    fontSize = 11.sp,
                    color = HangerPink,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append("VISTA SUA HISTÓRIA")
            }
        }
        Text(
            text = cityLine,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Edit button
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(36.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HangerInk)
        ) {
            Text(
                text = "Editar perfil",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HangerInk
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF999999)
        )
    }
}

// ─── Tab Row ──────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTabRow(
    selected: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(HangerCream)) {
        HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) HangerInk else HangerTextMuted,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(20.dp)
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(HangerInk)
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)
    }
}

// ─── Grid Item ────────────────────────────────────────────────────────────────

@Composable
private fun ProfileGridItem(post: PostDto) {
    val seed = post.id.hashCode() and 0x7FFFFFFF
    val (startColor, endColor) = HangerPlaceholderGradients[seed % HangerPlaceholderGradients.size]

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Brush.linearGradient(listOf(startColor, endColor)))
    ) {
        if (post.imageUrl.isNotBlank()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileEmptyState(
    emoji: String,
    message: String,
    sub: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 44.sp)
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = sub,
                fontSize = 12.sp,
                color = HangerTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
