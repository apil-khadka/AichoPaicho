package dev.nyxigale.aichopaicho.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.nyxigale.aichopaicho.data.AppDatabaseCallback
import dev.nyxigale.aichopaicho.data.dao.ContactDao
import dev.nyxigale.aichopaicho.data.dao.RecordDao
import dev.nyxigale.aichopaicho.data.dao.RepaymentDao
import dev.nyxigale.aichopaicho.data.dao.TypeDao
import dev.nyxigale.aichopaicho.data.dao.UserDao
import dev.nyxigale.aichopaicho.data.dao.UserRecordSummaryDao
import dev.nyxigale.aichopaicho.data.database.AppDatabase
import dev.nyxigale.aichopaicho.data.local.ScreenViewDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE records ADD COLUMN dueDate INTEGER")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
        typeDaoProvider: Provider<TypeDao>
    ): AppDatabase {
        return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "aichopaicho_app_database"
            )
            .addMigrations(MIGRATION_3_4)
            .fallbackToDestructiveMigration(true) // review
            .addCallback(AppDatabaseCallback(typeDaoProvider))
         .build()
    }

    @Provides
    @Singleton
    fun provideContactDao(appDatabase: AppDatabase): ContactDao {
        return appDatabase.contactDao()
    }

    @Provides
    @Singleton
    fun provideTypeDao(appDatabase: AppDatabase): TypeDao {
        return appDatabase.typeDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideRecordDao(appDatabase: AppDatabase): RecordDao {
        return appDatabase.recordDao()
    }

    @Provides
    @Singleton
    fun provideScreenViewDao(appDatabase: AppDatabase): ScreenViewDao {
        return appDatabase.screenViewDao()
    }

    @Provides
    @Singleton
    fun provideUserRecordSummaryDao(appDatabase: AppDatabase): UserRecordSummaryDao {
        return appDatabase.userRecordSummaryDao()
    }
    
    @Provides
    @Singleton
    fun provideRepaymentDao(appDatabase: AppDatabase): RepaymentDao {
        return appDatabase.repaymentDao()
    }
}
