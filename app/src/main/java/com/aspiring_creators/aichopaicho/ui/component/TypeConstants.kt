package com.aspiring_creators.aichopaicho.ui.component

object TypeConstants {
    const val TYPE_LENT = "Lent"
    const val LENT_ID = 1
    const val TYPE_BORROWED = "Borrowed"
    const val BORROWED_ID = 0

    fun getTypeName(value: Int): String {
        return when(value) {
            LENT_ID -> TYPE_LENT
            BORROWED_ID -> TYPE_BORROWED
            else -> "Unknown"
        }
    }
}
