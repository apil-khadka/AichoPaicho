package dev.nyxigale.aichopaicho.data.dto

data class ContactDto(
    val id: String = "",
    val name: String = "",
    val phone: String? = null,
    val contactId: String? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
