package dev.nyxigale.aichopaicho.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_views")
data class ScreenView(
    @PrimaryKey
    val screenId: String,
    val hasBeenShown: Boolean = false
)