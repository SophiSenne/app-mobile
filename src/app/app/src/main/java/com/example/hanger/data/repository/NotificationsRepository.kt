package com.hanger.app.data.repository

import com.hanger.app.data.model.NotificationDto
import com.hanger.app.data.network.ApiService
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class NotificationsResult {
    data class Success(val notifications: List<NotificationDto>) : NotificationsResult()
    data class Error(val message: String) : NotificationsResult()
}

class NotificationsRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun getNotifications(userId: String, limit: Int = 30, offset: Int = 0): NotificationsResult =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getNotifications(userId, limit, offset)
                if (response.isSuccessful) {
                    NotificationsResult.Success(response.body().orEmpty())
                } else {
                    NotificationsResult.Error("Erro ao carregar notificações (${response.code()})")
                }
            } catch (e: Exception) {
                NotificationsResult.Error(e.message ?: "Erro de conexão")
            }
        }

    suspend fun getUnreadCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val response = api.getUnreadNotificationsCount(userId)
            if (response.isSuccessful) response.body()?.count ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun markAllRead(userId: String): ActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.markAllNotificationsRead(userId)
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Erro ao marcar notificações (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Erro de conexão")
        }
    }
}
