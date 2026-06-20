package com.hanger.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Espelha o `PostDto` do contrato OpenAPI (Hanger API).
 *
 * Observação: a API não retorna avatar do autor dentro do PostDto (apenas
 * `username`). O avatar é resolvido client-side em [PostsRepository] via
 * cache de `GET /users/{userId}`, e exposto aqui através de [authorAvatarUrl]
 * (preenchido após o fetch, nullable).
 */
data class PostDto(
    val id: String,
    val userId: String,
    val username: String?,
    val imageUrl: String?,
    val title: String?,
    val caption: String?,
    val weatherCondition: String?,
    val temperature: Double?,
    val city: String?,
    val shareCount: Int,
    val createdAt: String,
    val categoryId: Int?,
    val categoryName: String?,
    val typeId: Int?,
    val typeName: String?,

    // ===== Campos client-side (preenchidos depois do fetch, não vêm da API) =====
    @SerializedName(value = "authorAvatarUrl", alternate = [])
    val authorAvatarUrl: String? = null,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val likesCount: Int = 0
) {
    /** Proporção usada para o efeito masonry no feed. Sem dado real de dimensão
     * da imagem ainda, então é derivada de forma estável a partir do [id]. */
    val imageAspectRatio: Float
        get() {
            val seed = id.hashCode() and 0x7FFFFFFF
            val ratios = floatArrayOf(0.74f, 0.95f, 1.05f, 0.85f, 1.15f)
            return ratios[seed % ratios.size]
        }
}

data class LikeDto(
    val userId: String,
    val username: String?,
    val postId: String,
    val createdAt: String
)