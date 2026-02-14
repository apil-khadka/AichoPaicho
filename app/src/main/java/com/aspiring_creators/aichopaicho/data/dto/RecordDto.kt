package com.aspiring_creators.aichopaicho.data.dto

data class RecordDto(
    val id: String = "",
    val userId: String? = null,
    val contactId: String? = null,
    val typeId: Int = 0,
    val amount: Int = 0,
    val date: Long = 0L,
    val isComplete: Boolean = false,
    val isDeleted: Boolean = false,
    val description: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
