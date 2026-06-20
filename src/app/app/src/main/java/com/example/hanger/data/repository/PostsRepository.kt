package com.hanger.app.data.repository

import com.hanger.app.data.model.PostDto
import com.hanger.app.data.network.ApiService
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

sealed class PostsResult {
    data class Success(val posts: List<PostDto>) : PostsResult()
    data class Error(val message: String) : PostsResult()
}

sealed class ActionResult {
    data object Success : ActionResult()
    data class Error(val message: String) : ActionResult()
}

class PostsRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    /** Cache simples em memória: userId -> avatarUrl. Evita repetir
     * GET /users/{userId} para autores que já aparecem em outros posts
     * da mesma página. */
    private val avatarCache = mutableMapOf<String, String?>()

    suspend fun getPosts(limit: Int, offset: Int): PostsResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getPosts(limit = limit, offset = offset)
            if (response.isSuccessful) {
                val posts = response.body().orEmpty()
                val withAuthors = hydrateAuthors(posts)
                val withLikes = hydrateLikesCount(withAuthors)
                PostsResult.Success(withLikes)
            } else {
                PostsResult.Error("Não foi possível carregar o feed (${response.code()})")
            }
        } catch (e: Exception) {
            PostsResult.Error(e.message ?: "Falha de conexão. Tente novamente.")
        }
    }

    /** Busca a contagem de curtidas de cada post em paralelo. O contrato não
     * expõe `likesCount` diretamente em `PostDto`, então complementamos com
     * `GET /posts/{postId}/likes/count`. */
    private suspend fun hydrateLikesCount(posts: List<PostDto>): List<PostDto> = coroutineScope {
        posts.map { post ->
            async {
                val count = try {
                    val response = api.getLikesCount(post.id)
                    if (response.isSuccessful) response.body() ?: 0 else 0
                } catch (e: Exception) {
                    0
                }
                post.copy(likesCount = count)
            }
        }.awaitAll()
    }

    /** Busca o avatar de cada autor único na lista, em paralelo,
     * reaproveitando o [avatarCache] entre chamadas. */
    private suspend fun hydrateAuthors(posts: List<PostDto>): List<PostDto> = coroutineScope {
        val uniqueUserIds = posts.map { it.userId }.distinct()
            .filter { !avatarCache.containsKey(it) }

        if (uniqueUserIds.isNotEmpty()) {
            uniqueUserIds.map { userId ->
                async {
                    val avatarUrl = try {
                        val response = api.getUser(userId)
                        if (response.isSuccessful) response.body()?.avatarUrl else null
                    } catch (e: Exception) {
                        null
                    }
                    userId to avatarUrl
                }
            }.awaitAll().forEach { (userId, avatarUrl) ->
                avatarCache[userId] = avatarUrl
            }
        }

        posts.map { post ->
            post.copy(authorAvatarUrl = avatarCache[post.userId])
        }
    }

    suspend fun like(postId: String, userId: String): ActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.likePost(postId, userId)
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Não foi possível curtir (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Falha de conexão.")
        }
    }

    suspend fun unlike(postId: String, userId: String): ActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.unlikePost(postId, userId)
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Não foi possível remover a curtida (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Falha de conexão.")
        }
    }
}