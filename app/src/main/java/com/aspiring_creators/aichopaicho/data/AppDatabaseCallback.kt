package com.aspiring_creators.aichopaicho.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class AppDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.beginTransaction()
        try {
            val timestamp = System.currentTimeMillis()
            db.execSQL("INSERT OR REPLACE INTO types (id, name, isDeleted, createdAt, updatedAt) VALUES (1, 'Lent', 0, $timestamp, $timestamp)")
            db.execSQL("INSERT OR REPLACE INTO types (id, name, isDeleted, createdAt, updatedAt) VALUES (0, 'Borrowed', 0, $timestamp, $timestamp)")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
