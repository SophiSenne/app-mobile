package com.hanger.app.data.model

data class NotificationDto(
    val id: String,
    val type: String,
    val actorId: String,
    val actorUsername: String,
    val actorAvatarUrl: String? = null,
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
