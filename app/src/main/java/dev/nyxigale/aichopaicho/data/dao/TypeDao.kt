package dev.nyxigale.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import dev.nyxigale.aichopaicho.data.entity.Type
import kotlinx.coroutines.flow.Flow

@Dao
interface TypeDao {

    @Upsert
    suspend fun upsert(type: Type)

    @Insert
    suspend fun insertAll(types: List<Type>)

    @Query("UPDATE types SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT * FROM types WHERE name = :name")
    suspend fun getByName(name: String): Type?

    @Query("SELECT * FROM types WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllTypes(): Flow<List<Type>>

    @Query("SELECT * FROM types WHERE id = :typeId AND isDeleted = 0")
    suspend fun getTypeById(typeId: Int): Type?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: Type)

}