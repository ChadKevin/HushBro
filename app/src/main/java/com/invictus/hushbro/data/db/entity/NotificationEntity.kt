package com.invictus.hushbro.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["key", "postTime", "textHash"], unique = true),
        Index(value = ["packageName", "postTime"]),
        Index(value = ["postTime"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String?,
    val text: String?,
    val postTime: Long,
    val tag: NotificationTag,
    val status: NotificationStatus,
    val isRead: Boolean = false,
    val isImportant: Boolean = false,
    val channelLabel: String? = null,
    val matchedRuleId: Long? = null,
    val textHash: String
)
