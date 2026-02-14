package com.aspiring_creators.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aspiring_creators.aichopaicho.data.entity.User

@Dao
interface UserDao {

    @Upsert
    suspend fun upsert(user: User)

//    @Query("SELECT * FROM users WHERE username = :username")
//    suspend fun getUserByUsername(username: String): User?


    @Query("UPDATE users SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT * FROM users WHERE isDeleted = 0 limit 1")
    suspend fun getUser( ): User

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserCompletely(userId: String)

}