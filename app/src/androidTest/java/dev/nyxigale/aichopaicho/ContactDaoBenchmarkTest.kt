package dev.nyxigale.aichopaicho

import android.content.Context
import android.util.Log
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
import kotlin.system.measureTimeMillis

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
    fun benchmarkReadsAndWrites() = runBlocking {
        val count = 1000
        val ids = mutableListOf<String>()
        val contacts = mutableListOf<Contact>()
        for (i in 1..count) {
            val id = "contact_$i"
            ids.add(id)
            contacts.add(Contact(id = id, name = "Name $i", userId = "user1", phone = emptyList(), contactId = "cid_$i"))
        }

        // Benchmark individual inserts
        val individualInsertTime = measureTimeMillis {
            for (contact in contacts) {
                contactDao.insertContact(contact)
            }
        }
        Log.i("Benchmark", "Individual inserts time for $count contacts: ${individualInsertTime}ms")

        // Benchmark individual reads
        val individualReadTime = measureTimeMillis {
            for (id in ids) {
                contactDao.getContactById(id)
            }
        }
        Log.i("Benchmark", "Individual reads time for $count contacts: ${individualReadTime}ms")

        // Clean up
        db.clearAllTables()
    }
}
