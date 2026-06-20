package com.hanger.app.ui.feed

import com.hanger.app.data.model.PostDto

data class FeedUiState(
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: FeedFilter = FeedFilter.ALL,
    val endReached: Boolean = false
)

enum class FeedFilter(val label: String) {
    ALL("Todos"),
    FOLLOWING("Seguindo"),
    CASUAL("Casual"),
    WORK("Trabalho"),
    PARTY("Festa"),
    SPORT("Esporte"),
    WINTER("Inverno")
}
