package com.invictus.hushbro.data.db.converter

import androidx.room.TypeConverter
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.db.model.RuleAction

class RoomConverters {

    @TypeConverter
    fun fromNotificationTag(tag: NotificationTag?): String? = tag?.name

    @TypeConverter
    fun toNotificationTag(value: String?): NotificationTag? =
        value?.let { runCatching { NotificationTag.valueOf(it) }.getOrDefault(NotificationTag.OTHER) }

    @TypeConverter
    fun fromNotificationStatus(status: NotificationStatus?): String? = status?.name

    @TypeConverter
    fun toNotificationStatus(value: String?): NotificationStatus? =
        value?.let { runCatching { NotificationStatus.valueOf(it) }.getOrDefault(NotificationStatus.KEPT) }

    @TypeConverter
    fun fromRuleAction(action: RuleAction?): String? = action?.name

    @TypeConverter
    fun toRuleAction(value: String?): RuleAction? =
        value?.let { runCatching { RuleAction.valueOf(it) }.getOrDefault(RuleAction.MUTE) }
}
