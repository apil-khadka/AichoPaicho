package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "repayments",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Loan::class, parentColumns = ["id"], childColumns = ["loanId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["userId"]), Index(value = ["loanId"])]
)
data class Repayment(
    @PrimaryKey val id: String, // UUID
    val userId: String?,
    val loanId: String?,
    val amountCents: Long, // Amount in cents
    val date: Long,
    val description: String?,
    @get:PropertyName("deleted")
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor(): this(
        id = "",
        userId = null,
        loanId = null,
        amountCents = 0L,
        date = 0L,
        description = null,
        isDeleted = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
