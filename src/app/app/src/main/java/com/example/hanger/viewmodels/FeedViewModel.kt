package com.hanger.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.data.repository.PostsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

class FeedViewModel(
    private val repository: PostsRepository
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
        // Filtro aplicado client-side por enquanto, pois o contrato atual de
        // GET /posts não expõe parâmetro de categoria. Quando o backend
        // suportar (ex. ?category=), trocar para recarregar via fetch().
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fetch(offset: Int, append: Boolean) {
        viewModelScope.launch {
            val result = repository.getPosts(limit = PAGE_SIZE, offset = offset)
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

    /** Atualiza otimisticamente o estado de curtida de um post (UI apenas). */
    fun toggleLike(postId: String) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == postId) {
                        val liked = !post.isLikedByMe
                        post.copy(
                            isLikedByMe = liked,
                            likesCount = post.likesCount + if (liked) 1 else -1
                        )
                    } else post
                }
            )
        }
        // TODO: chamar endpoint de like (ex. POST /posts/{id}/likes) quando publicado no contrato.
    }

    /** Atualiza otimisticamente o estado de salvo de um post (UI apenas). */
    fun toggleSave(postId: String) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == postId) post.copy(isSavedByMe = !post.isSavedByMe) else post
                }
            )
        }
        // TODO: chamar endpoint de save quando publicado no contrato.
    }
}