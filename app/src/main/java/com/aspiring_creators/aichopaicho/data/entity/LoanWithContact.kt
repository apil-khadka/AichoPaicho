package com.aspiring_creators.aichopaicho.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LoanWithContact(
    @Embedded val loan: Loan,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "id"
    )
    val contact: Contact?
)
