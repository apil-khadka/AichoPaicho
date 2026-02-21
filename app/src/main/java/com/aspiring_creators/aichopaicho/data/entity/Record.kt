package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Contact::class, parentColumns = ["id"], childColumns = ["contactId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Type::class, parentColumns = ["id"], childColumns = ["typeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["userId"]), Index(value = ["contactId"]), Index(value = ["typeId"])]
)
data class Record(
    @PrimaryKey val id: String,  // UUID
    val userId: String?,
    val contactId: String?,
    val typeId: Int,
    val amount: Int,
    val date: Long,
    val dueDate: Long? = null,
    @get:PropertyName("complete")
    val isComplete: Boolean = false,
    @get:PropertyName("deleted")
    val isDeleted: Boolean = false,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
){
    // Default constructor for Firestore/Room
    constructor(): this(
        id = "",
        userId = null,
        contactId = null,
        typeId = 0,
        amount = 0,
        date = 0L,
        dueDate = null,
        isComplete = false,
        isDeleted = false,
        description = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
