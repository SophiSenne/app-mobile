package com.hanger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.data.repository.WeatherRepository
import com.hanger.app.ui.feed.FeedFilter
import com.hanger.app.ui.feed.FeedViewModel
import com.hanger.app.ui.feed.WeatherViewModel
import com.hanger.app.ui.feed.components.FeedBottomNav
import com.hanger.app.ui.feed.components.FeedFilterRow
import com.hanger.app.ui.feed.components.FeedTopBar
import com.hanger.app.ui.feed.components.PostCard
import com.hanger.app.ui.feed.components.WeatherBottomSheet
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    userInitials: String = "ME",
    userId: String = "",
    onNavigateToProfile: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {}
) {
    val feedFactory = remember(userId) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FeedViewModel(PostsRepository(), currentUserId = userId) as T
        }
    }
    val weatherFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                WeatherViewModel(WeatherRepository()) as T
        }
    }

    val feedViewModel: FeedViewModel = viewModel(factory = feedFactory)
    val weatherViewModel: WeatherViewModel = viewModel(factory = weatherFactory)

    val feedState by feedViewModel.uiState.collectAsState()
    val weatherState by weatherViewModel.uiState.collectAsState()

    var showWeatherSheet by remember { mutableStateOf(false) }

    val displayedPosts = remember(feedState.posts, feedState.selectedFilter) {
        feedState.posts.filteredBy(feedState.selectedFilter)
    }

    Scaffold(
        containerColor = HangerCream,
        bottomBar = {
            FeedBottomNav(
                currentRoute = "feed",
                onHomeClick = {},
                onExploreClick = onNavigateToExplore,
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
            FeedTopBar(
                temperatureLabel = weatherState.snapshot?.let { "${it.currentTempC}°C" } ?: "—",
                userInitials = userInitials,
                onTemperatureClick = { showWeatherSheet = true },
                onProfileClick = onNavigateToProfile,
                onSearchClick = {}
            )

            HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)

            FeedFilterRow(
                selected = feedState.selectedFilter,
                onFilterSelected = feedViewModel::onFilterSelected
            )

            Box(modifier = Modifier.weight(1f)) {
                if (feedState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HangerPink)
                    }
                } else {
                    MasonryFeed(
                        posts = displayedPosts,
                        isRefreshing = feedState.isRefreshing,
                        isLoadingMore = feedState.isLoadingMore,
                        endReached = feedState.endReached,
                        selectedFilter = feedState.selectedFilter,
                        onRefresh = feedViewModel::refresh,
                        onLoadMore = feedViewModel::loadNextPage,
                        onSaveClick = feedViewModel::toggleSave,
                        onLikeClick = feedViewModel::toggleLike,
                        onPostClick = onNavigateToPost
                    )
                }

                feedState.errorMessage?.let { error ->
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
                        feedViewModel.dismissError()
                    }
                }
            }
        }
    }

    if (showWeatherSheet) {
        weatherState.snapshot?.let { snapshot ->
            WeatherBottomSheet(
                snapshot = snapshot,
                onDismiss = { showWeatherSheet = false }
            )
        }
    }
}

// ─── Masonry Feed ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MasonryFeed(
    posts: List<PostDto>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    endReached: Boolean,
    selectedFilter: FeedFilter,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onSaveClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onPostClick: (String) -> Unit = {}
) {
    val gridState = rememberLazyStaggeredGridState()
    val pullState = rememberPullToRefreshState()

    val shouldLoadMore by remember {
        derivedStateOf {
            if (endReached || isLoadingMore || posts.isEmpty()) return@derivedStateOf false
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= posts.size - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (posts.isEmpty() && !isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🪡", fontSize = 48.sp)
                    Text(
                        text = if (selectedFilter == FeedFilter.ALL) "Nenhum look encontrado"
                               else "Sem looks em \"${selectedFilter.label}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HangerInk,
                        modifier = Modifier.padding(top = 12.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Que tal postar o primeiro?",
                        fontSize = 12.sp,
                        color = HangerTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = posts,
                    key = { _, post -> post.id }
                ) { index, post ->
                    PostCard(
                        post = post,
                        indexForPlaceholder = index,
                        onClick = { onPostClick(post.id) },
                        onSaveClick = { onSaveClick(post.id) },
                        onLikeClick = { onLikeClick(post.id) }
                    )
                }

                if (isLoadingMore) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = HangerPink,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun List<PostDto>.filteredBy(filter: FeedFilter): List<PostDto> = when (filter) {
    FeedFilter.ALL       -> this
    FeedFilter.FOLLOWING -> this
    FeedFilter.CASUAL    -> this.filter { post ->
        post.categoryName?.contains("casual", ignoreCase = true) == true
    }
    FeedFilter.WORK      -> this.filter { post ->
        post.categoryName?.let { cn ->
            cn.contains("trabalho", ignoreCase = true) ||
            cn.contains("formal", ignoreCase = true) ||
            cn.contains("executiv", ignoreCase = true)
        } == true
    }
    FeedFilter.PARTY     -> this.filter { post ->
        post.categoryName?.let { cn ->
            cn.contains("festa", ignoreCase = true) ||
            cn.contains("noite", ignoreCase = true)
        } == true
    }
    FeedFilter.SPORT     -> this.filter { post ->
        post.categoryName?.let { cn ->
            cn.contains("esport", ignoreCase = true) ||
            cn.contains("academia", ignoreCase = true) ||
            cn.contains("fitness", ignoreCase = true)
        } == true
    }
    FeedFilter.WINTER    -> this.filter { post ->
        post.categoryName?.let { cn ->
            cn.contains("invern", ignoreCase = true) ||
            cn.contains("frio", ignoreCase = true)
        } == true
    }
}
