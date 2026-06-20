package com.hanger.app.data.model

import com.google.gson.annotations.SerializedName

// ===== Requests =====

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val locationCity: String = ""
)

data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)

// ===== Responses =====

data class User(
    val id: String,
    val username: String,
    val email: String,
    val bio: String?,
    val avatarUrl: String?,
    val locationCity: String?,
    val createdAt: String
)

data class AuthResponse(
    @SerializedName("user") val user: User
)

data class ApiError(
    val message: String? = null,
    val error: String? = null
)
