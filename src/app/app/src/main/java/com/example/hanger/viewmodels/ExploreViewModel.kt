package com.example.hanger.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.model.User
import com.hanger.app.data.network.RetrofitClient
import com.hanger.app.data.repository.PostsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExploreTab { POSTS, USERS }

data class ExploreUiState(
    val query: String = "",
    val activeTab: ExploreTab = ExploreTab.POSTS,
    val posts: List<PostDto> = emptyList(),
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)

class ExploreViewModel(
    private val postsRepository: PostsRepository = PostsRepository(),
    private val currentUserId: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val api = RetrofitClient.apiService
    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                posts = emptyList(),
                users = emptyList(),
                hasSearched = false,
                errorMessage = null
            )
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            search(query)
        }
    }

    fun onTabSelected(tab: ExploreTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { search(query) }
    }

    private suspend fun search(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val postsResp = api.searchPosts(query = query)
            val usersResp = api.searchUsers(query = query)

            val posts = if (postsResp.isSuccessful) postsResp.body().orEmpty() else emptyList()
            val users = if (usersResp.isSuccessful) usersResp.body().orEmpty() else emptyList()

            _uiState.value = _uiState.value.copy(
                posts = posts,
                users = users,
                isLoading = false,
                hasSearched = true
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Falha ao buscar. Verifique sua conexão.",
                hasSearched = true
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
