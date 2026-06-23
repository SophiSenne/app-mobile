package com.hanger.app.data.network

import com.hanger.app.data.model.AvatarUploadResponse
import com.hanger.app.data.model.CategoryDto
import com.hanger.app.data.model.CommentCountResponse
import com.hanger.app.data.model.CommentDto
import com.hanger.app.data.model.CreateCommentRequest
import com.hanger.app.data.model.HasLikedResponse
import com.hanger.app.data.model.HasSavedResponse
import com.hanger.app.data.model.LikeCountResponse
import com.hanger.app.data.model.LikeDto
import com.hanger.app.data.model.NotificationCountResponse
import com.hanger.app.data.model.NotificationDto
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.model.SavedPostDto
import com.hanger.app.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
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
    suspend fun getLikesCount(@Path("postId") postId: String): Response<LikeCountResponse>

    @GET("posts/{postId}/likes/check")
    suspend fun hasLikedPost(
        @Path("postId") postId: String,
        @Query("userId") userId: String
    ): Response<HasLikedResponse>

    // ===== Users (para resolver avatar do autor de cada post) =====

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>

    // ===== Saved Posts =====

    @POST("users/{userId}/saved-posts/{postId}")
    suspend fun savePost(
        @Path("userId") userId: String,
        @Path("postId") postId: String,
        @Header("X-User-Id") requestingUserId: String
    ): Response<SavedPostDto>

    @DELETE("users/{userId}/saved-posts/{postId}")
    suspend fun unsavePost(
        @Path("userId") userId: String,
        @Path("postId") postId: String,
        @Header("X-User-Id") requestingUserId: String
    ): Response<Unit>

    @GET("users/{userId}/saved-posts/{postId}")
    suspend fun hasSavedPost(
        @Path("userId") userId: String,
        @Path("postId") postId: String
    ): Response<HasSavedResponse>

    // ===== Comments =====

    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<CommentDto>>

    @GET("posts/{postId}/comments/count")
    suspend fun getCommentsCount(@Path("postId") postId: String): Response<CommentCountResponse>

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: String,
        @Header("X-User-Id") userId: String,
        @Body body: CreateCommentRequest
    ): Response<CommentDto>

    // ===== Upload =====

    @Multipart
    @POST("images/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<AvatarUploadResponse>

    // ===== Create Post =====

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Header("X-User-Id") userId: String,
        @Part image: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("caption") caption: RequestBody?,
        @Part("categoryId") categoryId: RequestBody?,
        @Part("typeId") typeId: RequestBody?,
        @Part("weatherCondition") weatherCondition: RequestBody?,
        @Part("temperature") temperature: RequestBody?,
        @Part("city") city: RequestBody?
    ): Response<PostDto>

    // ===== Notifications =====

    @GET("notifications")
    suspend fun getNotifications(
        @Header("X-User-Id") userId: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): Response<List<NotificationDto>>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationsCount(
        @Header("X-User-Id") userId: String
    ): Response<NotificationCountResponse>

    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Header("X-User-Id") userId: String,
        @Path("notificationId") notificationId: String
    ): Response<Unit>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Header("X-User-Id") userId: String
    ): Response<Unit>

    // ===== Search =====

    @GET("posts")
    suspend fun searchPosts(
        @Query("search") query: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): Response<List<PostDto>>

    @GET("users")
    suspend fun searchUsers(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<User>>
}