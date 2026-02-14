package com.aspiring_creators.aichopaicho.data.mapper

import com.aspiring_creators.aichopaicho.data.entity.User
import com.google.firebase.auth.FirebaseUser


fun FirebaseUser.toUserEntity() : User {
    return User(
        id = this.uid,
        email = this.email,
        name = this.displayName,
        photoUrl = this.photoUrl,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}