package com.aspiring_creators.aichopaicho.di

import com.aspiring_creators.aichopaicho.data.dao.ContactDao
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

//    @Binds
//    @Singleton
//    abstract fun bindContactRepository(
//        contactRepository: ContactRepository
//    ): ContactDao



}