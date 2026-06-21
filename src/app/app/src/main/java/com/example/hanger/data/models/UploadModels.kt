package com.hanger.app.data.model

import com.google.gson.annotations.SerializedName

data class AvatarUploadResponse(
    @SerializedName("avatarUrl") val avatarUrl: String
)
