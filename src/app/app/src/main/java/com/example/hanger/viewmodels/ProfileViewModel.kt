package com.hanger.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.data.repository.PostsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: PostsRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getPosts(limit = 100, offset = 0)
            when (result) {
                is PostsResult.Success -> {
                    val userPosts = result.posts.filter { it.userId == userId }
                    _uiState.update { it.copy(posts = userPosts, isLoading = false) }
                }
                is PostsResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
