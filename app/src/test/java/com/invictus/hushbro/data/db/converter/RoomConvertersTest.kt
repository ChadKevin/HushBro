package com.invictus.hushbro.data.db.converter

import com.google.common.truth.Truth.assertThat
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.db.model.RuleAction
import org.junit.Test

class RoomConvertersTest {

    private val converters = RoomConverters()

    @Test
    fun notificationTag_conversions() {
        for (tag in NotificationTag.entries) {
            val string = converters.fromNotificationTag(tag)
            val back = converters.toNotificationTag(string)
            assertThat(back).isEqualTo(tag)
        }
        assertThat(converters.fromNotificationTag(null)).isNull()
        assertThat(converters.toNotificationTag(null)).isNull()
        assertThat(converters.toNotificationTag("INVALID")).isEqualTo(NotificationTag.OTHER)
    }

    @Test
    fun notificationStatus_conversions() {
        for (status in NotificationStatus.entries) {
            val string = converters.fromNotificationStatus(status)
            val back = converters.toNotificationStatus(string)
            assertThat(back).isEqualTo(status)
        }
        assertThat(converters.fromNotificationStatus(null)).isNull()
        assertThat(converters.toNotificationStatus(null)).isNull()
        assertThat(converters.toNotificationStatus("INVALID")).isEqualTo(NotificationStatus.KEPT)
    }

    @Test
    fun ruleAction_conversions() {
        for (action in RuleAction.entries) {
            val string = converters.fromRuleAction(action)
            val back = converters.toRuleAction(string)
            assertThat(back).isEqualTo(action)
        }
        assertThat(converters.fromRuleAction(null)).isNull()
        assertThat(converters.toRuleAction(null)).isNull()
        assertThat(converters.toRuleAction("INVALID")).isEqualTo(RuleAction.MUTE)
    }
}
