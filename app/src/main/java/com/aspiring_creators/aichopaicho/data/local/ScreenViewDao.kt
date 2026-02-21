package com.aspiring_creators.aichopaicho.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ScreenViewDao {

    @Upsert
    suspend fun upsertScreenView(screenView: ScreenView)

    @Query("SELECT hasBeenShown FROM screen_views WHERE screenId = :screenId")
    suspend fun getScreenView(screenId: String): Boolean?
}