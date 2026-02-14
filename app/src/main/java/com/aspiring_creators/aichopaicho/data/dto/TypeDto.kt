package com.aspiring_creators.aichopaicho.data.dto

data class TypeDto(
    val id: Int = 0,
    val name: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)