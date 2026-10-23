package com.invictus.hushbro.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_prefs")
data class AppPrefEntity(
    @PrimaryKey
    val packageName: String,
    val isPrivate: Boolean = false,
    val digestEnabled: Boolean = true,
    val quietHoursStart: Int? = null,
    val quietHoursEnd: Int? = null
)
