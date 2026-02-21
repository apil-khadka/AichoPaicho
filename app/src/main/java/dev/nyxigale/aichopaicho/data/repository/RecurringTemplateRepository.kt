package dev.nyxigale.aichopaicho.data.repository

import dev.nyxigale.aichopaicho.data.dao.RecurringTemplateDao
import dev.nyxigale.aichopaicho.data.entity.RecurringTemplate
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RecurringTemplateRepository @Inject constructor(
    private val recurringTemplateDao: RecurringTemplateDao
) {
    suspend fun upsert(template: RecurringTemplate) {
        recurringTemplateDao.upsert(template)
    }

    fun getActiveTemplates(): Flow<List<RecurringTemplate>> {
        return recurringTemplateDao.getActiveTemplates()
    }

    suspend fun getDueTemplates(timestamp: Long): List<RecurringTemplate> {
        return recurringTemplateDao.getDueTemplates(timestamp)
    }

    suspend fun getAllTemplates(): List<RecurringTemplate> {
        return recurringTemplateDao.getAllTemplates()
    }

    suspend fun updateNextRun(templateId: String, nextRunAt: Long) {
        recurringTemplateDao.updateNextRun(
            templateId = templateId,
            nextRunAt = nextRunAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}
