package dev.nyxigale.aichopaicho.data.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    """
    SELECT
        r.userId AS user_id,
        COALESCE(SUM(CASE WHEN r.typeId = 1 THEN r.amount ELSE 0 END), 0) AS total_lent,
        COALESCE(SUM(CASE WHEN r.typeId = 0 THEN r.amount ELSE 0 END), 0) AS total_borrowed,
        COALESCE(SUM(CASE WHEN r.typeId = 1 THEN r.amount ELSE 0 END), 0)
          - COALESCE(SUM(CASE WHEN r.typeId = 0 THEN r.amount ELSE 0 END), 0) AS net_total,
          COUNT(DISTINCT CASE WHEN r.typeId = 1 THEN r.contactId ELSE NULL END) AS lent_contacts_count,
    COUNT(DISTINCT CASE WHEN r.typeId = 0 THEN r.contactId ELSE NULL END) AS borrowed_contacts_count
    FROM records r
    WHERE r.isDeleted = 0  AND r.isComplete = 0
    GROUP BY r.userId
"""
)
data class UserRecordSummary(
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "total_lent")
    val totalLent: Double,
    @ColumnInfo(name = "total_borrowed")
    val totalBorrowed: Double,
    @ColumnInfo(name="net_total")
    val netTotal: Double,
    @ColumnInfo(name = "lent_contacts_count")
    val lentContactsCount: Int ,
    @ColumnInfo(name = "borrowed_contacts_count")
    val borrowedContactsCount: Int
)