package com.hanger.app.data.repository

import android.content.Context
import android.net.Uri
import com.hanger.app.data.model.CommentDto
import com.hanger.app.data.model.CreateCommentRequest
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.model.SavedPostDto
import com.hanger.app.data.network.ApiService
import com.hanger.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    suspend fun getPosts(limit: Int, offset: Int, currentUserId: String = ""): PostsResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getPosts(limit = limit, offset = offset)
            if (response.isSuccessful) {
                val posts = response.body().orEmpty()
                val withAuthors = hydrateAuthors(posts)
                val withLikes = hydrateLikesCount(withAuthors)
                val withLikedStatus = if (currentUserId.isNotBlank()) hydrateLikedStatus(withLikes, currentUserId) else withLikes
                val withSaved = if (currentUserId.isNotBlank()) hydrateSavedStatus(withLikedStatus, currentUserId) else withLikedStatus
                PostsResult.Success(withSaved)
            } else {
                PostsResult.Error("Não foi possível carregar o feed (${response.code()})")
            }
        } catch (e: Exception) {
            PostsResult.Error(e.message ?: "Falha de conexão. Tente novamente.")
        }
    }

    /** Verifica em paralelo quais posts o usuário logado já curtiu. */
    private suspend fun hydrateLikedStatus(posts: List<PostDto>, userId: String): List<PostDto> = coroutineScope {
        posts.map { post ->
            async {
                val liked = try {
                    val response = api.hasLikedPost(post.id, userId)
                    if (response.isSuccessful) response.body()?.liked ?: false else false
                } catch (e: Exception) {
                    false
                }
                post.copy(isLikedByMe = liked)
            }
        }.awaitAll()
    }

    /** Verifica em paralelo quais posts já foram salvos pelo usuário logado. */
    private suspend fun hydrateSavedStatus(posts: List<PostDto>, userId: String): List<PostDto> = coroutineScope {
        posts.map { post ->
            async {
                val saved = try {
                    val response = api.hasSavedPost(userId, post.id)
                    if (response.isSuccessful) response.body()?.saved ?: false else false
                } catch (e: Exception) {
                    false
                }
                post.copy(isSavedByMe = saved)
            }
        }.awaitAll()
    }

    /** Busca a contagem de curtidas de cada post em paralelo. */
    private suspend fun hydrateLikesCount(posts: List<PostDto>): List<PostDto> = coroutineScope {
        posts.map { post ->
            async {
                val count = try {
                    val response = api.getLikesCount(post.id)
                    if (response.isSuccessful) response.body()?.count ?: 0 else 0
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

    suspend fun savePost(postId: String, userId: String): ActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.savePost(userId, postId, userId)
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Não foi possível salvar o post (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Falha de conexão.")
        }
    }

    suspend fun unsavePost(postId: String, userId: String): ActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.unsavePost(userId, postId, userId)
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Não foi possível remover o post salvo (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Falha de conexão.")
        }
    }

    suspend fun hasSavedPost(postId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.hasSavedPost(userId, postId)
            if (response.isSuccessful) response.body()?.saved ?: false else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPost(postId: String, currentUserId: String = ""): PostDto? = withContext(Dispatchers.IO) {
        try {
            val response = api.getPost(postId)
            if (!response.isSuccessful) return@withContext null
            var post = response.body() ?: return@withContext null
            val withAuthor = hydrateAuthors(listOf(post))
            post = withAuthor.first()
            val likesResp = api.getLikesCount(postId)
            if (likesResp.isSuccessful) post = post.copy(likesCount = likesResp.body()?.count ?: 0)
            if (currentUserId.isNotBlank()) {
                val likedResp = api.hasLikedPost(postId, currentUserId)
                if (likedResp.isSuccessful) post = post.copy(isLikedByMe = likedResp.body()?.liked ?: false)
                val savedResp = api.hasSavedPost(currentUserId, postId)
                if (savedResp.isSuccessful) post = post.copy(isSavedByMe = savedResp.body()?.saved ?: false)
            }
            post
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getComments(postId: String, limit: Int = 20, offset: Int = 0): List<CommentDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getComments(postId, limit, offset)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createComment(postId: String, userId: String, content: String): ActionResult = withContext(Dispatchers.IO) {
        try {
            val response = api.createComment(postId, userId, CreateCommentRequest(content))
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Não foi possível comentar (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Falha de conexão.")
        }
    }

    suspend fun createPost(
        userId: String,
        imageUri: Uri,
        context: Context,
        title: String,
        caption: String? = null,
        categoryId: Int? = null,
        typeId: Int? = null,
        weatherCondition: String? = null,
        temperature: Double? = null,
        city: String? = null
    ): ActionResult = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(imageUri) ?: "image/jpeg"
            val bytes = resolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return@withContext ActionResult.Error("Não foi possível ler a imagem")
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val imagePart = MultipartBody.Part.createFormData(
                name = "image",
                filename = "post.$extension",
                body = bytes.toRequestBody(mimeType.toMediaType())
            )
            val plain = "text/plain".toMediaTypeOrNull()
            val titlePart = title.toRequestBody(plain)
            val captionPart = caption?.toRequestBody(plain)
            val categoryPart = categoryId?.toString()?.toRequestBody(plain)
            val typePart = typeId?.toString()?.toRequestBody(plain)
            val conditionPart = weatherCondition?.toRequestBody(plain)
            val tempPart = temperature?.toString()?.toRequestBody(plain)
            val cityPart = city?.toRequestBody(plain)

            val response = api.createPost(
                userId = userId,
                image = imagePart,
                title = titlePart,
                caption = captionPart,
                categoryId = categoryPart,
                typeId = typePart,
                weatherCondition = conditionPart,
                temperature = tempPart,
                city = cityPart
            )
            if (response.isSuccessful) ActionResult.Success
            else ActionResult.Error("Não foi possível criar o post (${response.code()})")
        } catch (e: Exception) {
            ActionResult.Error(e.message ?: "Falha de conexão.")
        }
    }
}