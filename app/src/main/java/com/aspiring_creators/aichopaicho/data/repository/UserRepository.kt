package com.aspiring_creators.aichopaicho.data.repository

import com.aspiring_creators.aichopaicho.data.dao.UserDao
import com.aspiring_creators.aichopaicho.data.entity.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun upsert(user: User) {
        userDao.upsert(user)
    }

    suspend fun softDelete(id: String, updatedAt: Long) {
        userDao.softDelete(id, updatedAt)
    }

    suspend fun getUser(): User {
        return try {
            userDao.getUser()
        } catch (e: Exception) {
            // When no user is in the DB, Room throws an exception for a non-null return type.
            // We catch it and return a default "sentinel" user to avoid a crash.
            User(id = "", name = null, email = null, photoUrl = null, isOffline = true)
        }
    }

    suspend fun deleteUserCompletely(userId: String) {
        userDao.deleteUserCompletely(userId)
    }

}
