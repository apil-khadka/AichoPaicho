package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Contact::class, parentColumns = ["id"], childColumns = ["contactId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Type::class, parentColumns = ["id"], childColumns = ["typeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["userId"]), Index(value = ["contactId"]), Index(value = ["typeId"])]
)
data class Loan(
    @PrimaryKey val id: String, // UUID
    val userId: String?,
    val contactId: String?,
    val typeId: Int, // Lent or Borrowed
    val amount: Double, // Principal amount
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
        contactId = null,
        typeId = 0,
        amount = 0.0,
        date = 0L,
        description = null,
        isDeleted = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
