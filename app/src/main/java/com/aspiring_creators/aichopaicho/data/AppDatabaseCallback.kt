package com.aspiring_creators.aichopaicho.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aspiring_creators.aichopaicho.data.dao.TypeDao
import com.aspiring_creators.aichopaicho.data.entity.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider

class AppDatabaseCallback(
    private val typeDaoProvider: Provider<TypeDao>
) : RoomDatabase.Callback() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            populateInitialTypes()
        }
    }

    private suspend fun populateInitialTypes(){
        val typeDao = typeDaoProvider.get()

        val initialTypes = listOf(
            Type(id = 0, name = "Borrowed"),
            Type(id = 1, name = "Lent"),
        )
        typeDao.insertAll(initialTypes)
    }



}