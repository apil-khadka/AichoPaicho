package com.aspiring_creators.aichopaicho.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aspiring_creators.aichopaicho.data.local.ScreenView
import com.aspiring_creators.aichopaicho.data.local.ScreenViewDao

@Database(
    entities = [ScreenView::class],
    version = 1,
    exportSchema = false // Or your preference
)
abstract class ScreenViewDatabase : RoomDatabase() {
    abstract fun screenViewDao(): ScreenViewDao
}