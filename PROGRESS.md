# HushBro Progress

## Current Status

- Last completed milestone: Step 4 — Notification access and listener service.
- Current milestone: Step 4 complete. Ready for Step 5 — Buffered pipeline and batch writer.
- Overall status: In progress.
- GitHub repository: Step 1–3 changes pushed to GitHub (`main`). Step 4 implemented and tested locally.
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
  - [x] Committed and pushed Steps 1–3 to GitHub.
- [x] Step 4: Notification access and listener service:
  - [x] Created `HushBroListenerService` extending `NotificationListenerService`.
  - [x] Declared service in `AndroidManifest.xml` with `BIND_NOTIFICATION_LISTENER_SERVICE` permission and `android.service.notification.NotificationListenerService` intent action.
  - [x] Added package visibility `<queries>` for launcher applications.
  - [x] Created `ListenerBridge` singleton for lifecycle tracking, active notifications, and key cancellation.
  - [x] Filtered out HushBro's own notifications, ongoing notifications, and group summaries in `onNotificationPosted`.
  - [x] Extracted notification metadata safely with `BIG_TEXT` fallback to `TEXT`, and safe app label resolution.
  - [x] Logged safe non-sensitive metadata only (compliant with privacy constraints).
  - [x] Implemented rebind request in `onListenerDisconnected`.
  - [x] Created `PermissionUtils` with notification access verification and settings intent.
  - [x] Created `NotificationAccessBanner` with plain explanation (what is observed vs what is never done: offline, no network, no replies).
  - [x] Wired `MainActivity` to re-check access state on `ON_RESUME` and display the banner.

## Files Changed/Created in Step 4

- `app/src/main/AndroidManifest.xml` (added `<queries>` and declared `HushBroListenerService`)
- `app/src/main/java/com/invictus/hushbro/service/HushBroListenerService.kt`
- `app/src/main/java/com/invictus/hushbro/service/ListenerBridge.kt`
- `app/src/main/java/com/invictus/hushbro/util/PermissionUtils.kt`
- `app/src/main/java/com/invictus/hushbro/ui/onboarding/NotificationAccessBanner.kt`
- `app/src/main/java/com/invictus/hushbro/MainActivity.kt`

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
- Database: Room 2.6.1, version 1
- Notification listener: `HushBroListenerService` registered and active

## Issues / Notes

- Step 1–3 code is pushed to GitHub remote `origin/main`.
- Step 4 compiles and passes all tests via `.\gradlew.bat test assembleDebug`.

## Next Action

Step 5 — Buffered processing pipeline and batch writer (`RawNotification`, bounded Kotlin Channel, background consumer, deduplication, batch transaction insertion).

