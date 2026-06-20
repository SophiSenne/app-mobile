package com.hanger.app.data.model

data class CategoryDto(
    val id: Int,
    val name: String?,
    val types: List<TypeDto>? = null
)

data class TypeDto(
    val id: Int,
    val categoryId: Int,
    val categoryName: String?,
    val name: String?
)