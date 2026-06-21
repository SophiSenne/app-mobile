package com.hanger.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.repository.ActionResult
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.data.repository.PostsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

class FeedViewModel(
    private val repository: PostsRepository,
    private val currentUserId: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentOffset = 0

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        currentOffset = 0
        _uiState.update { it.copy(isLoading = true, errorMessage = null, endReached = false) }
        fetch(offset = 0, append = false)
    }

    fun refresh() {
        currentOffset = 0
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null, endReached = false) }
        fetch(offset = 0, append = false)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing || state.endReached) return
        _uiState.update { it.copy(isLoadingMore = true) }
        fetch(offset = currentOffset, append = true)
    }

    fun onFilterSelected(filter: FeedFilter) {
        if (filter == _uiState.value.selectedFilter) return
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fetch(offset: Int, append: Boolean) {
        viewModelScope.launch {
            val result = repository.getPosts(limit = PAGE_SIZE, offset = offset, currentUserId = currentUserId)
            when (result) {
                is PostsResult.Success -> {
                    val newPosts = result.posts
                    currentOffset = offset + newPosts.size
                    _uiState.update { current ->
                        current.copy(
                            posts = if (append) current.posts + newPosts else newPosts,
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            endReached = newPosts.size < PAGE_SIZE,
                            errorMessage = null
                        )
                    }
                }
                is PostsResult.Error -> {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /** Atualiza otimisticamente o estado de curtida e confirma via API. */
    fun toggleLike(postId: String) {
        if (currentUserId.isBlank()) return

        val post = _uiState.value.posts.find { it.id == postId } ?: return
        val nowLiked = !post.isLikedByMe

        _uiState.update { state ->
            state.copy(
                posts = state.posts.map {
                    if (it.id == postId) it.copy(
                        isLikedByMe = nowLiked,
                        likesCount = it.likesCount + if (nowLiked) 1 else -1
                    ) else it
                }
            )
        }

        viewModelScope.launch {
            val result = if (nowLiked) {
                repository.like(postId, currentUserId)
            } else {
                repository.unlike(postId, currentUserId)
            }

            if (result is ActionResult.Error) {
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.map {
                            if (it.id == postId) it.copy(
                                isLikedByMe = !nowLiked,
                                likesCount = it.likesCount + if (nowLiked) -1 else 1
                            ) else it
                        },
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /** Atualiza otimisticamente o estado de salvo e confirma via API. */
    fun toggleSave(postId: String) {
        if (currentUserId.isBlank()) return

        val post = _uiState.value.posts.find { it.id == postId } ?: return
        val nowSaved = !post.isSavedByMe

        // Atualização otimista imediata
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map {
                    if (it.id == postId) it.copy(isSavedByMe = nowSaved) else it
                }
            )
        }

        viewModelScope.launch {
            val result = if (nowSaved) {
                repository.savePost(postId, currentUserId)
            } else {
                repository.unsavePost(postId, currentUserId)
            }

            if (result is ActionResult.Error) {
                // Reverte em caso de falha
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.map {
                            if (it.id == postId) it.copy(isSavedByMe = !nowSaved) else it
                        },
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
