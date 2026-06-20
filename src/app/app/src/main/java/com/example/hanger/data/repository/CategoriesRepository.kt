package com.hanger.app.data.repository

import com.hanger.app.data.model.CategoryDto
import com.hanger.app.data.network.ApiService
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class CategoriesResult {
    data class Success(val categories: List<CategoryDto>) : CategoriesResult()
    data class Error(val message: String) : CategoriesResult()
}

class CategoriesRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun getCategories(): CategoriesResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            if (response.isSuccessful) {
                CategoriesResult.Success(response.body().orEmpty())
            } else {
                CategoriesResult.Error("Não foi possível carregar as categorias (${response.code()})")
            }
        } catch (e: Exception) {
            CategoriesResult.Error(e.message ?: "Falha de conexão.")
        }
    }
}