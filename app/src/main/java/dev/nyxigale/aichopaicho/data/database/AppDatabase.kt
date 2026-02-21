package dev.nyxigale.aichopaicho.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.nyxigale.aichopaicho.data.Converters
import dev.nyxigale.aichopaicho.data.dao.ContactDao
import dev.nyxigale.aichopaicho.data.dao.RecordDao
import dev.nyxigale.aichopaicho.data.dao.RepaymentDao
import dev.nyxigale.aichopaicho.data.dao.TypeDao
import dev.nyxigale.aichopaicho.data.dao.UserDao
import dev.nyxigale.aichopaicho.data.dao.UserRecordSummaryDao
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.Repayment
import dev.nyxigale.aichopaicho.data.entity.Type
import dev.nyxigale.aichopaicho.data.entity.User
import dev.nyxigale.aichopaicho.data.entity.UserRecordSummary
import dev.nyxigale.aichopaicho.data.local.ScreenView
import dev.nyxigale.aichopaicho.data.local.ScreenViewDao

@Database(
    entities = [
        Contact::class,
        Record::class,
        Type::class,
        User::class,
        ScreenView::class,
        Repayment::class
    ],
    views = [UserRecordSummary::class],
    version = 4, // Incremented version for schema change
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun recordDao(): RecordDao
    abstract fun typeDao(): TypeDao
    abstract fun userDao(): UserDao
    abstract fun screenViewDao(): ScreenViewDao
    abstract fun userRecordSummaryDao(): UserRecordSummaryDao
    abstract fun repaymentDao(): RepaymentDao
}
