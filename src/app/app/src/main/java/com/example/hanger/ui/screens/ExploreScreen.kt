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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerSurfaceAlt
import com.example.hanger.ui.theme.HangerTextMuted
import com.example.hanger.ui.theme.HangerTextSecondary
import com.example.hanger.viewmodels.ExploreTab
import com.example.hanger.viewmodels.ExploreViewModel
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.model.User
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.ui.feed.components.FeedBottomNav
import com.hanger.app.ui.feed.components.PostCard

@Composable
fun ExploreScreen(
    userId: String = "",
    userInitials: String = "ME",
    initialQuery: String = "",
    hasNotifications: Boolean = false,
    onNavigateToFeed: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {}
) {
    val factory = remember(userId) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExploreViewModel(PostsRepository(), currentUserId = userId) as T
        }
    }
    val vm: ExploreViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        if (initialQuery.isNotBlank()) {
            vm.onQueryChange(initialQuery)
        }
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = HangerCream,
        bottomBar = {
            FeedBottomNav(
                currentRoute = "explore",
                hasNotifications = hasNotifications,
                onHomeClick = onNavigateToFeed,
                onExploreClick = {},
                onCameraClick = onNavigateToCamera,
                onNotificationsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ─── Search Bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToFeed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = HangerInk
                    )
                }

                TextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    placeholder = {
                        Text(
                            text = "Buscar looks, pessoas...",
                            fontSize = 14.sp,
                            color = HangerTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = HangerTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.query.isNotBlank()) {
                            IconButton(onClick = { vm.onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Limpar",
                                    tint = HangerTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        vm.submitSearch()
                        keyboard?.hide()
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .clip(RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = HangerSurfaceAlt,
                        unfocusedContainerColor = HangerSurfaceAlt,
                        disabledContainerColor = HangerSurfaceAlt,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = HangerPink,
                        focusedTextColor = HangerInk,
                        unfocusedTextColor = HangerInk
                    )
                )
            }

            HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)

            // ─── Tabs (Posts / Usuários) ──────────────────────────────────────
            if (state.hasSearched || state.isLoading) {
                TabRow(
                    activeTab = state.activeTab,
                    onTabSelected = vm::onTabSelected
                )
                HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)
            }

            // ─── Content ─────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = HangerPink)
                        }
                    }

                    !state.hasSearched -> {
                        EmptySearchHint()
                    }

                    state.activeTab == ExploreTab.POSTS -> {
                        if (state.posts.isEmpty()) {
                            NoResults(label = "looks", query = state.query)
                        } else {
                            PostsGrid(
                                posts = state.posts,
                                onPostClick = onNavigateToPost
                            )
                        }
                    }

                    state.activeTab == ExploreTab.USERS -> {
                        if (state.users.isEmpty()) {
                            NoResults(label = "pessoas", query = state.query)
                        } else {
                            UsersList(users = state.users)
                        }
                    }
                }

                state.errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error,
                            fontSize = 12.sp,
                            color = HangerPink,
                            textAlign = TextAlign.Center
                        )
                    }
                    LaunchedEffect(error) {
                        kotlinx.coroutines.delay(4_000)
                        vm.dismissError()
                    }
                }
            }
        }
    }
}

// ─── Tab Row ─────────────────────────────────────────────────────────────────

@Composable
private fun TabRow(
    activeTab: ExploreTab,
    onTabSelected: (ExploreTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabChip(
            label = "Looks",
            selected = activeTab == ExploreTab.POSTS,
            onClick = { onTabSelected(ExploreTab.POSTS) }
        )
        TabChip(
            label = "Pessoas",
            selected = activeTab == ExploreTab.USERS,
            onClick = { onTabSelected(ExploreTab.USERS) }
        )
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) HangerInk else HangerSurfaceAlt)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else HangerTextMuted
        )
    }
}

// ─── Posts Grid (masonry 2 cols) ─────────────────────────────────────────────

@Composable
private fun PostsGrid(
    posts: List<PostDto>,
    onPostClick: (String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 10.dp, end = 10.dp, top = 8.dp, bottom = 16.dp
        ),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items = posts, key = { _, post -> post.id }) { index, post ->
            PostCard(
                post = post,
                indexForPlaceholder = index,
                onClick = { onPostClick(post.id) },
                onSaveClick = {},
                onLikeClick = {}
            )
        }
    }
}

// ─── Users List ──────────────────────────────────────────────────────────────

@Composable
private fun UsersList(users: List<User>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = users, key = { it.id }) { user ->
            UserRow(user = user)
            HorizontalDivider(
                color = Color(0x0D000000),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun UserRow(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.avatarUrl != null) {
            AsyncImage(
                model = user.avatarUrl,
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
                    .background(HangerPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.take(2).uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "@${user.username}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!user.bio.isNullOrBlank()) {
                Text(
                    text = user.bio,
                    fontSize = 12.sp,
                    color = HangerTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (!user.locationCity.isNullOrBlank()) {
                Text(
                    text = user.locationCity,
                    fontSize = 11.sp,
                    color = HangerTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = HangerTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Empty States ─────────────────────────────────────────────────────────────

@Composable
private fun EmptySearchHint() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = HangerTextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Explore looks e pessoas",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Digite um título, estilo ou nome de usuário para buscar",
                fontSize = 13.sp,
                color = HangerTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun NoResults(label: String, query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "🔍", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nenhum resultado",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Não encontramos $label para \"$query\"",
                fontSize = 13.sp,
                color = HangerTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
