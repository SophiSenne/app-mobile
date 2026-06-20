package com.hanger.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.model.User
import com.hanger.app.data.repository.AuthRepository
import com.hanger.app.data.repository.AuthResult
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun login(emailOrUsername: String, password: String) {
        if (emailOrUsername.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Preencha e-mail/usuário e senha")
            return
        }
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.login(emailOrUsername, password)) {
                is AuthResult.Success -> uiState = uiState.copy(
                    isLoading = false,
                    loggedInUser = result.user
                )
                is AuthResult.Error -> uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        acceptedTerms: Boolean,
        bio: String = "",
        avatarUrl: String = "",
        locationCity: String = ""
    ) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Preencha todos os campos obrigatórios")
            return
        }
        if (password.length < 8) {
            uiState = uiState.copy(errorMessage = "A senha deve ter no mínimo 8 caracteres")
            return
        }
        if (!acceptedTerms) {
            uiState = uiState.copy(errorMessage = "Você precisa aceitar os Termos de Uso")
            return
        }
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.register(
                username = username,
                email = email,
                password = password,
                bio = bio,
                avatarUrl = avatarUrl,
                locationCity = locationCity
            )) {
                is AuthResult.Success -> uiState = uiState.copy(
                    isLoading = false,
                    loggedInUser = result.user
                )
                is AuthResult.Error -> uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}
