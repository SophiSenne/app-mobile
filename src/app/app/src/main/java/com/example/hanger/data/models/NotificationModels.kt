package com.hanger.app.data.model

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: String,
    val type: String,
    val recipientId: String,
    @SerializedName("senderId") val actorId: String,
    @SerializedName("senderUsername") val actorUsername: String,
    @SerializedName("senderAvatarUrl") val actorAvatarUrl: String? = null,
    val postId: String? = null,
    val postImageUrl: String? = null,
    val commentContent: String? = null,
    val read: Boolean,
    val createdAt: String
)

enum class NotificationType(val value: String) {
    LIKE("like"),
    COMMENT("comment"),
    FOLLOW("follow"),
    UNKNOWN("unknown");

    companion object {
        fun from(value: String) = entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}

data class NotificationCountResponse(
    val count: Int
)
