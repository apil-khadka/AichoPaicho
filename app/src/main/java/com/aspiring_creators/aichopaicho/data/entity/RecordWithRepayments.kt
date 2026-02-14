package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RecordWithRepayments(
    @Embedded val record: Record,
    @Relation(
        parentColumn = "id",
        entityColumn = "recordId"
    )
    val repayments: List<Repayment>
) {
    val totalRepayment: Int
        get() = repayments.sumOf { it.amount }

    val remainingAmount: Int
        get() = record.amount - totalRepayment

    val isSettled: Boolean
        get() = remainingAmount <= 0
}
