package com.hanger.app.data.repository

import android.content.Context
import android.net.Uri
import com.hanger.app.data.network.ApiService
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class UploadResult {
    data class Success(val url: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

class UserRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun uploadAvatar(uri: Uri, context: Context): UploadResult =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext UploadResult.Error("Não foi possível ler a imagem")

                val extension = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }
                val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                val part = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = "avatar.$extension",
                    body = requestBody
                )

                val response = api.uploadAvatar(part)
                if (response.isSuccessful) {
                    val url = response.body()?.avatarUrl
                        ?: return@withContext UploadResult.Error("Resposta inválida do servidor")
                    UploadResult.Success(url)
                } else {
                    UploadResult.Error("Erro no upload (${response.code()})")
                }
            } catch (e: Exception) {
                UploadResult.Error(e.message ?: "Falha no upload. Tente novamente.")
            }
        }
}
