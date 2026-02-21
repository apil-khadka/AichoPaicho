package dev.nyxigale.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "types")
data class Type(
    @PrimaryKey val id: Int ,
    val name: String,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)