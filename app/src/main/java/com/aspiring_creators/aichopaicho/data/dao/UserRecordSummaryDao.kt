package com.aspiring_creators.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRecordSummaryDao {
    @Query("SELECT * FROM UserRecordSummary limit 1")
     fun getCurrentUserSummary():Flow<UserRecordSummary?>

    @Query(
        """
        SELECT DISTINCT
            urs.user_id, 
            urs.total_lent, 
            urs.total_borrowed,
            urs.net_total,
            urs.lent_contacts_count,
            urs.borrowed_contacts_count
        FROM UserRecordSummary urs
        INNER JOIN records r ON urs.user_id = r.userId 
        WHERE r.date BETWEEN :startDate AND :endDate AND r.isDeleted = 0 AND isComplete = 0
        LIMIT 1 
    """
    )
     fun getCurrentUserSummaryByDateRange(startDate: Long, endDate: Long): Flow<UserRecordSummary?>

}