package com.aspiring_creators.aichopaicho.di

import android.content.Context
import androidx.room.Room
import com.aspiring_creators.aichopaicho.data.AppDatabaseCallback
import com.aspiring_creators.aichopaicho.data.dao.ContactDao
import com.aspiring_creators.aichopaicho.data.dao.RecordDao
import com.aspiring_creators.aichopaicho.data.dao.RepaymentDao
import com.aspiring_creators.aichopaicho.data.dao.TypeDao
import com.aspiring_creators.aichopaicho.data.dao.UserDao
import com.aspiring_creators.aichopaicho.data.dao.UserRecordSummaryDao
import com.aspiring_creators.aichopaicho.data.database.AppDatabase
import com.aspiring_creators.aichopaicho.data.local.ScreenViewDao
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
            ).fallbackToDestructiveMigration(true) // review
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
