package com.example.hanger.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.model.NotificationDto
import com.hanger.app.data.model.NotificationType
import com.hanger.app.data.repository.NotificationsRepository
import com.hanger.app.data.repository.NotificationsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val unreadCount: Int = 0
)

class NotificationsViewModel(
    private val userId: String,
    private val repository: NotificationsRepository = NotificationsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getNotifications(userId)) {
                is NotificationsResult.Success -> {
                    val unread = result.notifications.count { !it.read }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = result.notifications,
                            unreadCount = unread
                        )
                    }
                }
                is NotificationsResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = repository.getNotifications(userId)) {
                is NotificationsResult.Success -> {
                    val unread = result.notifications.count { !it.read }
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            notifications = result.notifications,
                            unreadCount = unread
                        )
                    }
                }
                is NotificationsResult.Error -> {
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markAllRead(userId)
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map { it.copy(read = true) },
                    unreadCount = 0
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
