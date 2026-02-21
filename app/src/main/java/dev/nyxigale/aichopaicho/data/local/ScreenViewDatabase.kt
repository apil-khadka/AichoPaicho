package dev.nyxigale.aichopaicho.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScreenView::class],
    version = 1,
    exportSchema = false // Or your preference
)
abstract class ScreenViewDatabase : RoomDatabase() {
    abstract fun screenViewDao(): ScreenViewDao
}