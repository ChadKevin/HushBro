package com.invictus.hushbro.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.invictus.hushbro.data.db.model.RuleAction

@Entity(
    tableName = "rules",
    indices = [
        Index(value = ["priority"]),
        Index(value = ["enabled"])
    ]
)
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val priority: Int,
    val appPackage: String? = null,
    val keyword: String,
    val action: RuleAction,
    val channelLabel: String? = null,
    val enabled: Boolean = true
)
