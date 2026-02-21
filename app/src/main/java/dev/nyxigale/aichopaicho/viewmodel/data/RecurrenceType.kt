package dev.nyxigale.aichopaicho.viewmodel.data

enum class RecurrenceType(val intervalDays: Int?) {
    NONE(null),
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30),
    CUSTOM(null)
}
