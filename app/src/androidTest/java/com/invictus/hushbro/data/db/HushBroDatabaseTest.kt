package com.invictus.hushbro.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.invictus.hushbro.data.db.dao.AppPrefDao
import com.invictus.hushbro.data.db.dao.NotificationDao
import com.invictus.hushbro.data.db.dao.RuleDao
import com.invictus.hushbro.data.db.entity.AppPrefEntity
import com.invictus.hushbro.data.db.entity.NotificationEntity
import com.invictus.hushbro.data.db.entity.RuleEntity
import com.invictus.hushbro.data.db.model.NotificationStatus
import com.invictus.hushbro.data.db.model.NotificationTag
import com.invictus.hushbro.data.db.model.RuleAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HushBroDatabaseTest {

    private lateinit var db: HushBroDatabase
    private lateinit var notificationDao: NotificationDao
    private lateinit var ruleDao: RuleDao
    private lateinit var appPrefDao: AppPrefDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HushBroDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        notificationDao = db.notificationDao()
        ruleDao = db.ruleDao()
        appPrefDao = db.appPrefDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun createTestNotification(
        id: Long = 0L,
        key: String = "test_key_1",
        packageName: String = "com.example.chat",
        appLabel: String = "ChatApp",
        title: String? = "Alice",
        text: String? = "Hello there",
        postTime: Long = 1000L,
        tag: NotificationTag = NotificationTag.SOCIAL,
        status: NotificationStatus = NotificationStatus.KEPT,
        isRead: Boolean = false,
        isImportant: Boolean = false,
        channelLabel: String? = null,
        matchedRuleId: Long? = null,
        textHash: String = "hash123"
    ): NotificationEntity {
        return NotificationEntity(
            id = id,
            key = key,
            packageName = packageName,
            appLabel = appLabel,
            title = title,
            text = text,
            postTime = postTime,
            tag = tag,
            status = status,
            isRead = isRead,
            isImportant = isImportant,
            channelLabel = channelLabel,
            matchedRuleId = matchedRuleId,
            textHash = textHash
        )
    }

    @Test
    fun insertDuplicateNotification_isIgnored_confirmsOneRow() = runTest {
        val notification1 = createTestNotification(
            key = "dedupe_key",
            postTime = 5000L,
            textHash = "unique_hash_1"
        )
        val notification2 = createTestNotification(
            key = "dedupe_key",
            postTime = 5000L,
            textHash = "unique_hash_1"
        )

        val id1 = notificationDao.insert(notification1)
        assertThat(id1).isGreaterThan(0L)

        // Attempt to insert duplicate with same (key, postTime, textHash)
        val id2 = notificationDao.insert(notification2)
        assertThat(id2).isEqualTo(-1L)

        // Confirm only one row exists
        val count = notificationDao.getCount()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun batchInsert_handlesDuplicatesGracefully() = runTest {
        val n1 = createTestNotification(key = "k1", postTime = 100L, textHash = "h1")
        val n2 = createTestNotification(key = "k2", postTime = 200L, textHash = "h2")
        val nDuplicate = createTestNotification(key = "k1", postTime = 100L, textHash = "h1")

        val inserted = notificationDao.insertBatch(listOf(n1, n2, nDuplicate))
        assertThat(inserted[0]).isGreaterThan(0L)
        assertThat(inserted[1]).isGreaterThan(0L)
        assertThat(inserted[2]).isEqualTo(-1L)

        assertThat(notificationDao.getCount()).isEqualTo(2)
    }

    @Test
    fun searchQuery_returnsOnlyMatchingRows() = runTest {
        val item1 = createTestNotification(
            key = "k1", postTime = 100L, textHash = "h1",
            title = "Special Discount", text = "Get 50% off today", appLabel = "ShopNow"
        )
        val item2 = createTestNotification(
            key = "k2", postTime = 200L, textHash = "h2",
            title = "Security Alert", text = "Your OTP is 123456", appLabel = "BankSafe"
        )
        val item3 = createTestNotification(
            key = "k3", postTime = 300L, textHash = "h3",
            title = "Alice sent a photo", text = "Check out vacation pictures", appLabel = "SocialNet"
        )

        notificationDao.insertBatch(listOf(item1, item2, item3))

        // Search by title
        val titleMatch = notificationDao.search("Discount").first()
        assertThat(titleMatch).hasSize(1)
        assertThat(titleMatch[0].appLabel).isEqualTo("ShopNow")

        // Search by text
        val textMatch = notificationDao.search("OTP").first()
        assertThat(textMatch).hasSize(1)
        assertThat(textMatch[0].appLabel).isEqualTo("BankSafe")

        // Search by app label
        val appMatch = notificationDao.search("SocialNet").first()
        assertThat(appMatch).hasSize(1)
        assertThat(appMatch[0].title).isEqualTo("Alice sent a photo")

        // Search with non-matching query
        val emptyMatch = notificationDao.search("NonExistentQuery").first()
        assertThat(emptyMatch).isEmpty()
    }

    @Test
    fun daoOrdering_isNewestFirst() = runTest {
        val older = createTestNotification(key = "k1", postTime = 1000L, textHash = "h1")
        val middle = createTestNotification(key = "k2", postTime = 2000L, textHash = "h2")
        val newest = createTestNotification(key = "k3", postTime = 3000L, textHash = "h3")

        // Insert in arbitrary order
        notificationDao.insertBatch(listOf(older, newest, middle))

        val all = notificationDao.getAllFlow().first()
        assertThat(all).hasSize(3)
        assertThat(all[0].postTime).isEqualTo(3000L)
        assertThat(all[1].postTime).isEqualTo(2000L)
        assertThat(all[2].postTime).isEqualTo(1000L)
    }

    @Test
    fun markRead_and_clearOperations_behaveCorrectly() = runTest {
        val n1 = createTestNotification(key = "k1", packageName = "app.a", postTime = 100L, textHash = "h1", isRead = false)
        val n2 = createTestNotification(key = "k2", packageName = "app.a", postTime = 200L, textHash = "h2", isRead = false)
        val n3 = createTestNotification(key = "k3", packageName = "app.b", postTime = 300L, textHash = "h3", isRead = false)

        val insertedIds = notificationDao.insertBatch(listOf(n1, n2, n3))

        // Mark single notification read
        notificationDao.markRead(insertedIds[0])
        val updatedFirst = notificationDao.getById(insertedIds[0])
        assertThat(updatedFirst?.isRead).isTrue()

        // Mark all for app.a read
        notificationDao.markAppRead("app.a")
        val appANotifs = notificationDao.getByApp("app.a").first()
        assertThat(appANotifs.all { it.isRead }).isTrue()

        // Clear app.a
        notificationDao.clearApp("app.a")
        assertThat(notificationDao.getByApp("app.a").first()).isEmpty()
        assertThat(notificationDao.getByApp("app.b").first()).hasSize(1)

        // Clear all
        notificationDao.clearAll()
        assertThat(notificationDao.getCount()).isEqualTo(0)
    }

    @Test
    fun statusUpdate_and_heldForDigest_workAsExpected() = runTest {
        val n1 = createTestNotification(key = "k1", postTime = 1000L, textHash = "h1", status = NotificationStatus.HELD)
        val n2 = createTestNotification(key = "k2", postTime = 2000L, textHash = "h2", status = NotificationStatus.HELD)
        val n3 = createTestNotification(key = "k3", postTime = 3000L, textHash = "h3", status = NotificationStatus.KEPT)

        val ids = notificationDao.insertBatch(listOf(n1, n2, n3))

        val heldUpTo1500 = notificationDao.getHeldForDigest(1500L)
        assertThat(heldUpTo1500).hasSize(1)
        assertThat(heldUpTo1500[0].key).isEqualTo("k1")

        // Update status of held items to DIGESTED
        notificationDao.updateStatus(ids[0], NotificationStatus.DIGESTED)
        val afterUpdate = notificationDao.getById(ids[0])
        assertThat(afterUpdate?.status).isEqualTo(NotificationStatus.DIGESTED)
    }

    @Test
    fun ruleDao_orderedByPriority_and_swapPriority() = runTest {
        val rule1 = RuleEntity(id = 1L, priority = 1, keyword = "promo", action = RuleAction.MUTE)
        val rule2 = RuleEntity(id = 2L, priority = 2, keyword = "urgent", action = RuleAction.MARK_IMPORTANT)

        ruleDao.upsert(rule1)
        ruleDao.upsert(rule2)

        val initialRules = ruleDao.observeRules().first()
        assertThat(initialRules[0].priority).isEqualTo(1)
        assertThat(initialRules[1].priority).isEqualTo(2)

        // Swap priority
        ruleDao.swapPriority(id1 = 1L, priority1 = 1, id2 = 2L, priority2 = 2)

        val swappedRules = ruleDao.observeRules().first()
        assertThat(swappedRules[0].id).isEqualTo(2L)
        assertThat(swappedRules[0].priority).isEqualTo(1)
        assertThat(swappedRules[1].id).isEqualTo(1L)
        assertThat(swappedRules[1].priority).isEqualTo(2)
    }

    @Test
    fun appPrefDao_upsert_and_retrieve() = runTest {
        val pref = AppPrefEntity(
            packageName = "com.example.secret",
            isPrivate = true,
            digestEnabled = false,
            quietHoursStart = 1320, // 22:00
            quietHoursEnd = 420     // 07:00
        )

        appPrefDao.upsert(pref)

        val retrieved = appPrefDao.getPreferenceSync("com.example.secret")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.isPrivate).isTrue()
        assertThat(retrieved?.digestEnabled).isFalse()
        assertThat(retrieved?.quietHoursStart).isEqualTo(1320)
        assertThat(retrieved?.quietHoursEnd).isEqualTo(420)
    }
}
