package dev.nyxigale.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.nyxigale.aichopaicho.data.entity.RecurringTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTemplateDao {
    @Upsert
    suspend fun upsert(template: RecurringTemplate)

    @Query("SELECT * FROM recurring_templates WHERE isActive = 1 ORDER BY nextRunAt ASC")
    fun getActiveTemplates(): Flow<List<RecurringTemplate>>

    @Query("SELECT * FROM recurring_templates WHERE isActive = 1 AND nextRunAt <= :timestamp ORDER BY nextRunAt ASC")
    suspend fun getDueTemplates(timestamp: Long): List<RecurringTemplate>

    @Query("SELECT * FROM recurring_templates ORDER BY createdAt DESC")
    suspend fun getAllTemplates(): List<RecurringTemplate>

    @Query("UPDATE recurring_templates SET nextRunAt = :nextRunAt, updatedAt = :updatedAt WHERE id = :templateId")
    suspend fun updateNextRun(templateId: String, nextRunAt: Long, updatedAt: Long)
}
