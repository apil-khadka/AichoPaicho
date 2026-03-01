package com.aspiring_creators.aichopaicho

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.nyxigale.aichopaicho.data.database.AppDatabase
import dev.nyxigale.aichopaicho.data.dao.ContactDao
import dev.nyxigale.aichopaicho.data.entity.Contact
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log

@RunWith(AndroidJUnit4::class)
class ContactDaoBenchmarkTest {
    private lateinit var db: AppDatabase
    private lateinit var contactDao: ContactDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        contactDao = db.contactDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun benchmarkReads() = runBlocking {
        // Insert 1000 contacts
        val ids = mutableListOf<String>()
        for (i in 1..1000) {
            val id = "contact_$i"
            ids.add(id)
            contactDao.insertContact(Contact(id = id, name = "Name $i", userId = "user1", phone = emptyList(), contactId = "cid_$i"))
        }

        // Measure individual reads
        val startTime = System.currentTimeMillis()
        for (id in ids) {
            contactDao.getContactById(id)
        }
        val individualTime = System.currentTimeMillis() - startTime
        Log.i("Benchmark", "Individual reads time: ${individualTime}ms")

        // We can't measure batch read yet until we implement it
    }
}
