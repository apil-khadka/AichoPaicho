package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LoanWithRepayments(
    @Embedded val loan: Loan,
    @Relation(
        parentColumn = "id",
        entityColumn = "loanId"
    )
    val repayments: List<Repayment>
)
