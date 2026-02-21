package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Record

/**
 * Lightweight dashboard item for due dates.
 */
data class UpcomingDueItem(
    val recordId: String,
    val contactName: String,
    val amount: Int,
    val dueDate: Long,
    val typeId: Int
) {
    companion object {
        fun from(record: Record, contactName: String): UpcomingDueItem? {
            val due = record.dueDate ?: return null
            return UpcomingDueItem(
                recordId = record.id,
                contactName = contactName,
                amount = record.amount,
                dueDate = due,
                typeId = record.typeId
            )
        }
    }
}
