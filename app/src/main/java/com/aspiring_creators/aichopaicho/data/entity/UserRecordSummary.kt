package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    viewName = "user_record_summary",
    value = """
    SELECT
        u.id AS user_id,
        (
            COALESCE((SELECT SUM(amountCents) FROM loans WHERE userId = u.id AND typeId = 1 AND isDeleted = 0), 0) -
            COALESCE((SELECT SUM(r.amountCents) FROM repayments r JOIN loans l ON r.loanId = l.id WHERE l.userId = u.id AND l.typeId = 1 AND r.isDeleted = 0), 0)
        ) / 100.0 AS total_lent,
        (
            COALESCE((SELECT SUM(amountCents) FROM loans WHERE userId = u.id AND typeId = 0 AND isDeleted = 0), 0) -
            COALESCE((SELECT SUM(r.amountCents) FROM repayments r JOIN loans l ON r.loanId = l.id WHERE l.userId = u.id AND l.typeId = 0 AND r.isDeleted = 0), 0)
        ) / 100.0 AS total_borrowed,
        (
            (
                COALESCE((SELECT SUM(amountCents) FROM loans WHERE userId = u.id AND typeId = 1 AND isDeleted = 0), 0) -
                COALESCE((SELECT SUM(r.amountCents) FROM repayments r JOIN loans l ON r.loanId = l.id WHERE l.userId = u.id AND l.typeId = 1 AND r.isDeleted = 0), 0)
            ) -
            (
                COALESCE((SELECT SUM(amountCents) FROM loans WHERE userId = u.id AND typeId = 0 AND isDeleted = 0), 0) -
                COALESCE((SELECT SUM(r.amountCents) FROM repayments r JOIN loans l ON r.loanId = l.id WHERE l.userId = u.id AND l.typeId = 0 AND r.isDeleted = 0), 0)
            )
        ) / 100.0 AS net_total,
        (SELECT COUNT(DISTINCT contactId) FROM loans WHERE userId = u.id AND typeId = 1 AND isDeleted = 0) AS lent_contacts_count,
        (SELECT COUNT(DISTINCT contactId) FROM loans WHERE userId = u.id AND typeId = 0 AND isDeleted = 0) AS borrowed_contacts_count
    FROM users u
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
