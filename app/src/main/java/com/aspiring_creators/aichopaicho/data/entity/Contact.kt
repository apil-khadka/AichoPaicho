package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "contacts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["externalRef"], unique = true),
        Index(value = ["normalizedPhone"]),
        Index(value = ["userId"])
    ]
)
data class Contact(
    @PrimaryKey val id: String, // Internal UUID
    val name: String,
    val userId: String?,
    val phone: List<String?> ,
    val normalizedPhone: String?, // For deduplication
    val externalRef: String?, // External ID (e.g. Google Contact ID)
    @get:PropertyName("deleted")
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
){
     constructor(): this(
        id = "",
        name = "",
        userId = null,
        phone = emptyList(),
        normalizedPhone = null,
        externalRef = null,
        isDeleted = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
