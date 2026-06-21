package com.hanger.app.data.network

import com.hanger.app.data.model.AvatarUploadResponse
import com.hanger.app.data.model.CategoryDto
import com.hanger.app.data.model.LikeDto
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoints consumidos pela tela de Feed: posts, categorias (filtros) e likes.
 */
interface ApiService {

    // ===== Posts =====

    @GET("posts")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<PostDto>>

    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: String): Response<PostDto>

    // ===== Categories (usadas como filtros do feed) =====

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    // ===== Likes =====

    @POST("posts/{postId}/likes")
    suspend fun likePost(
        @Path("postId") postId: String,
        @Header("X-User-Id") userId: String
    ): Response<LikeDto>

    @DELETE("posts/{postId}/likes")
    suspend fun unlikePost(
        @Path("postId") postId: String,
        @Header("X-User-Id") userId: String
    ): Response<Unit>

    @GET("posts/{postId}/likes/count")
    suspend fun getLikesCount(@Path("postId") postId: String): Response<Int>

    // ===== Users (para resolver avatar do autor de cada post) =====

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>

    // ===== Upload =====

    @Multipart
    @POST("images/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<AvatarUploadResponse>
}