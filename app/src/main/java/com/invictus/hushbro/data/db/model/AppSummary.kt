package com.invictus.hushbro.data.db.model

data class AppSummary(
    val packageName: String,
    val appLabel: String,
    val totalCount: Int,
    val unreadCount: Int,
    val latestPostTime: Long
)
