# HushBro Progress

## Current Status

- Last completed milestone: Step 3 — Repositories, dependency injection and channels.
- Current milestone: Step 3 complete. Ready for Step 4 — Notification access and listener service.
- Overall status: In progress.
- GitHub repository: Project has been pushed to GitHub.
- Last verified build: `.\gradlew.bat test assembleDebug` succeeded with exit code 0.

## Completed

- [x] Step 0: Created the Android Studio project named HushBro.
- [x] Step 1: Project setup and app shell verification (`minSdk = 26`, `android:allowBackup="false"`, no `INTERNET` permission).
- [x] Step 2: Room database implementation:
  - [x] Added KSP `2.0.21-1.0.28`, Room `2.6.1`, Paging 3, Google Truth, and coroutines testing dependencies.
  - [x] Created `NotificationTag` (`OTP`, `PROMO`, `SOCIAL`, `OTHER`) and `NotificationStatus` (`KEPT`, `MUTED`, `HELD`, `DIGESTED`, `SNOOZED`) enums.
  - [x] Created `RuleAction` enum (`MUTE`, `MARK_IMPORTANT`, `ROUTE`).
  - [x] Created `AppSummary` aggregation data class.
  - [x] Created `RoomConverters` with fallback handling.
  - [x] Created `NotificationEntity` with unique deduplication index (`key`, `postTime`, `textHash`), app/time index, and time index.
  - [x] Created `RuleEntity` with priority index and enabled index.
  - [x] Created `AppPrefEntity` for per-app privacy and quiet hours settings.
  - [x] Created `NotificationDao` with duplicate-safe batch inserts (`OnConflictStrategy.IGNORE`), paged search, app summaries, per-app paging, read/clear, status update, held-for-digest, and pruning queries.
  - [x] Created `RuleDao` with priority observation, upsert, delete, and priority swap transaction.
  - [x] Created `AppPrefDao` with observe all, upsert, and get operations.
  - [x] Created `HushBroDatabase` (version 1, schema export enabled, destructive migration fallback for dev).
  - [x] Generated Room schema v1 (`app/schemas/com.invictus.hushbro.data.db.HushBroDatabase/1.json`).
  - [x] Created unit tests (`RoomConvertersTest`) and instrumented test suite (`HushBroDatabaseTest`).
- [x] Step 3: Repositories, dependency injection (Hilt), DataStore preferences, and notification channels:
  - [x] Configured Dagger Hilt `2.51.1` and `androidx.datastore:datastore-preferences:1.1.3`.
  - [x] Created `Channels` helper registering `hushbro_digest` (IMPORTANCE_LOW) and `hushbro_snooze` (IMPORTANCE_DEFAULT) notification channels.
  - [x] Created `HushBroApp` (`Application`) annotated with `@HiltAndroidApp` initializing notification channels at startup.
  - [x] Registered `android:name=".HushBroApp"` in `AndroidManifest.xml`.
  - [x] Created `SettingsStore` managing DataStore preferences (`onboardingDone`, `digestInterval`, `digestTags`, `quietHoursEnabled`, `quietHoursStart`, `quietHoursEnd`, `retentionDays`).
  - [x] Created repositories: `NotificationRepository`, `RuleRepository`, `StatsRepository`, and `AppPrefRepository`.
  - [x] Created Hilt DI modules: `DatabaseModule`, `DataStoreModule`, and `CoroutineModule` (providing application-scoped `CoroutineScope`).
  - [x] Annotated `MainActivity` with `@AndroidEntryPoint` and injected `NotificationRepository` and `SettingsStore`.
  - [x] Created unit tests (`NotificationRepositoryTest`).

## Files Changed/Created in Step 3

- `gradle/libs.versions.toml` (added Hilt and DataStore dependencies)
- `build.gradle.kts` (added Hilt plugin alias)
- `app/build.gradle.kts` (applied Hilt plugin, dependencies)
- `app/src/main/AndroidManifest.xml` (registered `android:name=".HushBroApp"`)
- `app/src/main/java/com/invictus/hushbro/HushBroApp.kt`
- `app/src/main/java/com/invictus/hushbro/notify/Channels.kt`
- `app/src/main/java/com/invictus/hushbro/data/prefs/SettingsStore.kt`
- `app/src/main/java/com/invictus/hushbro/data/repo/NotificationRepository.kt`
- `app/src/main/java/com/invictus/hushbro/data/repo/RuleRepository.kt`
- `app/src/main/java/com/invictus/hushbro/data/repo/StatsRepository.kt`
- `app/src/main/java/com/invictus/hushbro/data/repo/AppPrefRepository.kt`
- `app/src/main/java/com/invictus/hushbro/di/DatabaseModule.kt`
- `app/src/main/java/com/invictus/hushbro/di/DataStoreModule.kt`
- `app/src/main/java/com/invictus/hushbro/di/CoroutineModule.kt`
- `app/src/main/java/com/invictus/hushbro/MainActivity.kt`
- `app/src/test/java/com/invictus/hushbro/data/repo/NotificationRepositoryTest.kt`

## Current Known Configuration

- Language: Kotlin 2.0.21
- AGP: 8.11.2
- Gradle: 8.13 (running on Android Studio bundled JBR 21)
- UI: Jetpack Compose with Material 3 (Compose BOM 2025.02.00)
- DI: Hilt 2.51.1
- Preferences: DataStore Preferences 1.1.3
- Package/Application ID: `com.invictus.hushbro`
- Compile SDK: 36
- Target SDK: 36
- Minimum SDK: 26
- Backup allowed: false
- Internet permission: None
- Database: Room 2.6.1, version 1, initial unencrypted implementation
- Notification listener: Not implemented yet (scheduled for Step 4)
- Navigation: Not implemented yet (scheduled for Step 9)

## Issues / Notes

- Running `.\gradlew.bat test assembleDebug` succeeds cleanly with exit code 0 (79 actionable tasks).
- Remember to click **Sync Now** in Android Studio so the IDE editor indexes the new Hilt and DataStore libraries.

## Next Action

Step 4 — Notification access and listener service. Do not start until approved.
