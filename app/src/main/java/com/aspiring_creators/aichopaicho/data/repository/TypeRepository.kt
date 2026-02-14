package com.aspiring_creators.aichopaicho.data.repository

import com.aspiring_creators.aichopaicho.data.dao.TypeDao
import com.aspiring_creators.aichopaicho.data.entity.Type
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TypeRepository @Inject constructor(private val typeDao: TypeDao) {

    suspend fun upsert(type: Type) {
        typeDao.upsert(type)
    }

    suspend fun softDelete(id: String, updatedAt: Long) {
        typeDao.softDelete(id, updatedAt)
    }

    suspend fun getByName(name: String): Type? {
        return typeDao.getByName(name)
    }

    fun getAllTypes(): Flow<List<Type>> {
        return typeDao.getAllTypes()
    }

    suspend fun getTypeById(typeId: Int): Type? {
        return typeDao.getTypeById(typeId)
    }

    suspend fun insertType(type: Type) {
        typeDao.insertType(type)
    }

}