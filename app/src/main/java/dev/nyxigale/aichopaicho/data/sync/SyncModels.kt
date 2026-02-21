package dev.nyxigale.aichopaicho.data.sync

enum class SyncEntityType {
    CONTACT,
    RECORD,
    REPAYMENT,
    USER
}

data class SyncFailureItem(
    val entityType: SyncEntityType,
    val entityId: String,
    val reason: String? = null
) {
    fun key(): String = "${entityType.name}:$entityId"
}

data class SyncReport(
    val attempted: Int = 0,
    val succeeded: Int = 0,
    val failed: Int = 0,
    val failedItems: List<SyncFailureItem> = emptyList()
) {
    operator fun plus(other: SyncReport): SyncReport {
        return SyncReport(
            attempted = attempted + other.attempted,
            succeeded = succeeded + other.succeeded,
            failed = failed + other.failed,
            failedItems = failedItems + other.failedItems
        )
    }

    companion object {
        val EMPTY = SyncReport()
    }
}
