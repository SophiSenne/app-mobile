package com.hanger.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.model.CommentDto
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.repository.ActionResult
import com.hanger.app.data.repository.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val post: PostDto? = null,
    val comments: List<CommentDto> = emptyList(),
    val isLoadingPost: Boolean = true,
    val isLoadingComments: Boolean = false,
    val isSendingComment: Boolean = false,
    val errorMessage: String? = null
)

class PostDetailViewModel(
    private val repository: PostsRepository,
    private val postId: String,
    private val currentUserId: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadPost()
        loadComments()
    }

    fun loadPost() {
        _uiState.update { it.copy(isLoadingPost = true, errorMessage = null) }
        viewModelScope.launch {
            val post = repository.getPost(postId, currentUserId)
            _uiState.update { it.copy(post = post, isLoadingPost = false) }
        }
    }

    fun loadComments() {
        _uiState.update { it.copy(isLoadingComments = true) }
        viewModelScope.launch {
            val comments = repository.getComments(postId)
            _uiState.update { it.copy(comments = comments, isLoadingComments = false) }
        }
    }

    fun sendComment(content: String) {
        if (currentUserId.isBlank() || content.isBlank()) return
        _uiState.update { it.copy(isSendingComment = true) }
        viewModelScope.launch {
            when (val result = repository.createComment(postId, currentUserId, content)) {
                is ActionResult.Success -> {
                    val updated = repository.getComments(postId)
                    _uiState.update { it.copy(comments = updated, isSendingComment = false) }
                }
                is ActionResult.Error -> {
                    _uiState.update { it.copy(isSendingComment = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun toggleLike() {
        val post = _uiState.value.post ?: return
        if (currentUserId.isBlank()) return
        val nowLiked = !post.isLikedByMe
        _uiState.update { state ->
            state.copy(
                post = post.copy(
                    isLikedByMe = nowLiked,
                    likesCount = post.likesCount + if (nowLiked) 1 else -1
                )
            )
        }
        viewModelScope.launch {
            val result = if (nowLiked) repository.like(post.id, currentUserId)
                         else repository.unlike(post.id, currentUserId)
            if (result is ActionResult.Error) {
                _uiState.update { state ->
                    state.copy(
                        post = state.post?.copy(
                            isLikedByMe = !nowLiked,
                            likesCount = (state.post.likesCount) + if (nowLiked) -1 else 1
                        ),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun toggleSave() {
        val post = _uiState.value.post ?: return
        if (currentUserId.isBlank()) return
        val nowSaved = !post.isSavedByMe
        _uiState.update { state -> state.copy(post = post.copy(isSavedByMe = nowSaved)) }
        viewModelScope.launch {
            val result = if (nowSaved) repository.savePost(post.id, currentUserId)
                         else repository.unsavePost(post.id, currentUserId)
            if (result is ActionResult.Error) {
                _uiState.update { state ->
                    state.copy(
                        post = state.post?.copy(isSavedByMe = !nowSaved),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
