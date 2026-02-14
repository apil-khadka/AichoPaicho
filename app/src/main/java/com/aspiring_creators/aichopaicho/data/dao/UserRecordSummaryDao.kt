package com.aspiring_creators.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRecordSummaryDao {
    @Query("SELECT * FROM user_record_summary limit 1")
    fun getCurrentUserSummary():Flow<UserRecordSummary?>
}
