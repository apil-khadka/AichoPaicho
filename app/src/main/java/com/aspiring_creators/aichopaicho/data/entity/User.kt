package com.aspiring_creators.aichopaicho.data.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,  // Firebase UID or UUID
    val name: String?,
    val email: String?,
    val photoUrl: Uri?,
    val isDeleted: Boolean = false,
    val isOffline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

