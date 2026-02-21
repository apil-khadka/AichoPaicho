package dev.nyxigale.aichopaicho.data.repository

import dev.nyxigale.aichopaicho.data.dao.UserRecordSummaryDao
import dev.nyxigale.aichopaicho.data.entity.UserRecordSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRecordSummaryRepository @Inject constructor(
    private val userRecordSummaryDao: UserRecordSummaryDao
) {

     fun getCurrentUserSummary(): Flow<UserRecordSummary?> {
        return userRecordSummaryDao.getCurrentUserSummary()
    }

     fun getCurrentUserSummaryByDate(start: Long, end: Long): Flow<UserRecordSummary?> {
        return userRecordSummaryDao.getCurrentUserSummaryByDateRange(start, end)
    }

}