package dev.nyxigale.aichopaicho.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "repayments",
    foreignKeys = [
        ForeignKey(
            entity = Record::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE // If a record is deleted, its repayments are also deleted
        )
    ],
    indices = [Index(value = ["recordId"])]
)
data class Repayment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recordId: String,
    val amount: Int, // Stored in cents, like the Record amount
    val date: Long,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
