package dev.nyxigale.aichopaicho.data.dto

import android.net.Uri
import com.google.firebase.auth.FirebaseUser


data class UserDto(
    val id: String = "",
    val name: String? = null,
    val email: String? = null,
    val photoUrl: Uri? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
){
    constructor(firebaseUser: FirebaseUser): this(
        id = firebaseUser.uid,
        email = firebaseUser.email,
        name = firebaseUser.displayName,
        photoUrl = firebaseUser.photoUrl,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}





