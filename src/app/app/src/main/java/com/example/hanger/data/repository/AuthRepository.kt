package com.hanger.app.data.repository

import com.google.gson.Gson
import com.hanger.app.data.model.ApiError
import com.hanger.app.data.model.LoginRequest
import com.hanger.app.data.model.RegisterRequest
import com.hanger.app.data.model.User
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Resultado simples para a UI não depender de exceptions/Retrofit diretamente. */
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val api: AuthApiService = RetrofitClient.authApiService
) {
    suspend fun login(emailOrUsername: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(emailOrUsername, password))
                if (response.isSuccessful) {
                    response.body()?.user?.let { AuthResult.Success(it) }
                        ?: AuthResult.Error("Resposta vazia do servidor")
                } else {
                    AuthResult.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Falha de conexão. Tente novamente.")
            }
        }

    suspend fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        bio: String = "",
        avatarUrl: String = "",
        locationCity: String = ""
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = api.register(
                RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    email = email,
                    password = password,
                    bio = bio,
                    avatarUrl = avatarUrl,
                    locationCity = locationCity
                )
            )
            if (response.isSuccessful) {
                response.body()?.user?.let { AuthResult.Success(it) }
                    ?: AuthResult.Error("Resposta vazia do servidor")
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Falha de conexão. Tente novamente.")
        }
    }

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Erro inesperado. Tente novamente."
        return try {
            val apiError = Gson().fromJson(body, ApiError::class.java)
            apiError.message ?: apiError.error ?: "Erro inesperado. Tente novamente."
        } catch (e: Exception) {
            "Erro inesperado. Tente novamente."
        }
    }
}

// import necessário para o tipo usado acima (mantido aqui para evitar import cíclico no topo)
private typealias AuthApiService = com.hanger.app.data.network.AuthApiService
