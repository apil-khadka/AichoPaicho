package dev.nyxigale.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_templates",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Contact::class, parentColumns = ["id"], childColumns = ["contactId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Type::class, parentColumns = ["id"], childColumns = ["typeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["contactId"]),
        Index(value = ["typeId"]),
        Index(value = ["nextRunAt"])
    ]
)
data class RecurringTemplate(
    @PrimaryKey val id: String,
    val userId: String?,
    val contactId: String?,
    val typeId: Int,
    val amount: Int,
    val description: String?,
    val intervalDays: Int,
    val nextRunAt: Long,
    val dueOffsetDays: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
