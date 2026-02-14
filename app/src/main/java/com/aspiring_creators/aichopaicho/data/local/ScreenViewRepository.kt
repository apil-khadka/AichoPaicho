package com.aspiring_creators.aichopaicho.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenViewRepository @Inject constructor(private val screenViewDao: ScreenViewDao) {

    suspend fun getScreenView(screenId: String): Boolean? {
        val result = screenViewDao.getScreenView(screenId)
        return result
    }

     suspend fun markScreenAsShown(screenId: String) {
        screenViewDao.upsertScreenView(ScreenView(screenId = screenId, hasBeenShown = true))
    }
}