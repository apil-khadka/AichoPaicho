package com.aspiring_creators.aichopaicho.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppDatabaseCallback : RoomDatabase.Callback() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            db.execSQL("INSERT OR REPLACE INTO types (id, name, isDeleted, createdAt, updatedAt) VALUES (1, 'Lent', 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})")
            db.execSQL("INSERT OR REPLACE INTO types (id, name, isDeleted, createdAt, updatedAt) VALUES (2, 'Borrowed', 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})")
        }
    }
}
