package dev.nyxigale.aichopaicho.data.entity

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
    indices = [Index(value = ["contactId"],  unique = true) , Index(value = ["userId"])]
)
data class Contact(
    @PrimaryKey val id: String,
    val name: String,
    val userId: String?,
    val phone: List<String?> ,
    val contactId: String?,
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
        contactId = null,
        isDeleted = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
