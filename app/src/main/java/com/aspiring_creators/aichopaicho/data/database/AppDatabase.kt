package com.aspiring_creators.aichopaicho.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aspiring_creators.aichopaicho.data.Converters
import com.aspiring_creators.aichopaicho.data.dao.ContactDao
import com.aspiring_creators.aichopaicho.data.dao.LoanDao
import com.aspiring_creators.aichopaicho.data.dao.RepaymentDao
import com.aspiring_creators.aichopaicho.data.dao.TypeDao
import com.aspiring_creators.aichopaicho.data.dao.UserDao
import com.aspiring_creators.aichopaicho.data.dao.UserRecordSummaryDao
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import com.aspiring_creators.aichopaicho.data.entity.Type
import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary
import com.aspiring_creators.aichopaicho.data.local.ScreenView
import com.aspiring_creators.aichopaicho.data.local.ScreenViewDao

@Database(
    entities = [
        Contact::class,
        Loan::class,
        Repayment::class,
        Type::class,
        User::class,
        ScreenView::class
    ],
    views = [UserRecordSummary::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun typeDao(): TypeDao
    abstract fun userDao(): UserDao
    abstract fun screenViewDao(): ScreenViewDao

    abstract fun userRecordSummaryDao(): UserRecordSummaryDao
}