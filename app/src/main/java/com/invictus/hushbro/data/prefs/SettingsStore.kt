package com.invictus.hushbro.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hushbro_settings")

data class UserSettings(
    val onboardingDone: Boolean = false,
    val digestInterval: String = "DAILY",
    val digestTags: Set<String> = setOf("PROMO", "SOCIAL"),
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 1320, // 22:00
    val quietHoursEnd: Int = 420,    // 07:00
    val retentionDays: Int = 30
)

class SettingsStore(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_DIGEST_INTERVAL = stringPreferencesKey("digest_interval")
        val KEY_DIGEST_TAGS = stringSetPreferencesKey("digest_tags")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val KEY_QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val KEY_QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val KEY_RETENTION_DAYS = intPreferencesKey("retention_days")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                onboardingDone = preferences[KEY_ONBOARDING_DONE] ?: false,
                digestInterval = preferences[KEY_DIGEST_INTERVAL] ?: "DAILY",
                digestTags = preferences[KEY_DIGEST_TAGS] ?: setOf("PROMO", "SOCIAL"),
                quietHoursEnabled = preferences[KEY_QUIET_HOURS_ENABLED] ?: false,
                quietHoursStart = preferences[KEY_QUIET_HOURS_START] ?: 1320,
                quietHoursEnd = preferences[KEY_QUIET_HOURS_END] ?: 420,
                retentionDays = preferences[KEY_RETENTION_DAYS] ?: 30
            )
        }

    val onboardingDoneFlow: Flow<Boolean> = userSettingsFlow.map { it.onboardingDone }
    val digestIntervalFlow: Flow<String> = userSettingsFlow.map { it.digestInterval }
    val digestTagsFlow: Flow<Set<String>> = userSettingsFlow.map { it.digestTags }
    val retentionDaysFlow: Flow<Int> = userSettingsFlow.map { it.retentionDays }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_DONE] = done
        }
    }

    suspend fun setDigestInterval(interval: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DIGEST_INTERVAL] = interval
        }
    }

    suspend fun setDigestTags(tags: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DIGEST_TAGS] = tags
        }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_QUIET_HOURS_ENABLED] = enabled
        }
    }

    suspend fun setQuietHours(startMinutes: Int, endMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_QUIET_HOURS_START] = startMinutes
            preferences[KEY_QUIET_HOURS_END] = endMinutes
        }
    }

    suspend fun setRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RETENTION_DAYS] = days
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
