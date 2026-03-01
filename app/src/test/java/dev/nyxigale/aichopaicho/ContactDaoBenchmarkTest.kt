package dev.nyxigale.aichopaicho

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.nyxigale.aichopaicho.data.database.AppDatabase
import dev.nyxigale.aichopaicho.data.dao.ContactDao
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
class ContactDaoBenchmarkTest {
    private lateinit var db: AppDatabase
    private lateinit var contactDao: ContactDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        contactDao = db.contactDao()
        runBlocking {
            db.userDao().upsert(User(id = "user1", email = "test@test.com", photoUrl = null, name = "User 1"))
        }
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun benchmarkBatchReadsAndWrites() = runBlocking {
        val count = 1000
        val ids = mutableListOf<String>()
        val contacts = mutableListOf<Contact>()
        for (i in 1..count) {
            val id = "contact_$i"
            ids.add(id)
            contacts.add(Contact(id = id, name = "Name $i", userId = "user1", phone = emptyList(), contactId = "cid_$i"))
        }

        // Clean up
        db.clearAllTables()
        db.userDao().upsert(User(id = "user1", email = "test@test.com", photoUrl = null, name = "User 1"))

        // Benchmark individual inserts
        val individualInsertTime = measureTimeMillis {
            for (contact in contacts) {
                contactDao.insertContact(contact)
            }
        }
        println("Benchmark: Individual inserts time for $count contacts: ${individualInsertTime}ms")

        // Benchmark individual reads
        val individualReadTime = measureTimeMillis {
            for (id in ids) {
                contactDao.getContactById(id)
            }
        }
        println("Benchmark: Individual reads time for $count contacts: ${individualReadTime}ms")

        // Clean up
        db.clearAllTables()
    }
}
