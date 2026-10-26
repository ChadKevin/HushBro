# HushBro Progress

## Current Status

- Last completed milestone: Step 4 — Notification access and listener service.
- Current milestone: Step 4 complete. Ready for Step 5 — Buffered pipeline and batch writer.
- Overall status: In progress.

## Live notification access fix
- Made `HushBroListenerService` back-fill robust (wrapped in try/catch, refreshes active count).
- Stopped dropping ongoing/live notifications in `toRawNotification` so currently active notifications are captured.
- Hardened `ListenerBridge.getActiveNotifications()` with runCatching.
- Added service rebind + active-count refresh in `MainActivity.onResume()` when access is granted but listener not connected.
- Build: assembleDebug PASSED, testDebugUnitTest PASSED.
- GitHub repository: Step 1–3 changes pushed to GitHub (`main`). Step 4 implemented and tested locally.
- Last verified build: `.\gradlew.bat test assembleDebug` succeeded with exit code 0.
- Follow-up: Fixed broken AutumnTheme/AutumnComponents compile errors and applied AutumnTheme in MainActivity. Build and unit tests pass.


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

## Step 5 Completion

- Implemented buffered `NotificationPipeline` with bounded `Channel` (capacity 512, DROP_OLDEST), background consumer, batch writer (max 100), drain timeout, dedup identity, processing errors skipped.
- Made `ListenerBridge` testable and `HushBroListenerService` enqueue/backfill via pipeline.
- Fixed Android `Log` calls to be unit-test safe.
- Lazy-started consumer after enqueue to preserve bounded overflow semantics.
- Tests: `:app:testDebugUnitTest` passes (19 tests). Build: `:app:compileDebugKotlin` passes.

## Next Action

Step 6 — (next milestone).


- **BUG FIXED: App crash on startup caused by unhandled Error during SQLCipher database initialization.**
  - Root cause: HushBroDatabase.getInstance() used catch (e: Exception) which does NOT catch java.lang.Error subclasses like UnsatisfiedLinkError. When SQLCipher's native library fails to load during database initialization, an UnsatisfiedLinkError (an Error, not Exception) is thrown, propagates uncaught, and crashes the app on startup.
  - Fix: Changed catch (e: Exception) to catch (e: Throwable) in HushBroDatabase.getInstance() so both Exception and Error types are caught. If SQLCipher initialization fails, the app falls back to plain Room instead of crashing.

- **BUG FIX: App crash on startup � multiple startup resilience improvements.**
  Root cause: App crashes on startup when SQLCipher native library (libsqlcipher.so) fails to load, throwing UnsatisfiedLinkError (a java.lang.Error, not Exception). The original catch (e: Exception) in HushBroDatabase.getInstance() did not catch Error types.
  Fixes applied:
  1. HushBroApp.onCreate() � wrapped Channels.createNotificationChannels() in try-catch so notification channel setup can never crash the Application.
  2. HushBroDatabase.getInstance() � changed catch (e: Exception) to catch (e: Throwable) to catch Error types like UnsatisfiedLinkError.
  3. HushBroDatabase.providePlainRoomFallback() � new method with its own try-catch and final retry, so even if plain Room fails, the app attempts recovery before giving up.

- **ROOT CAUSE FOUND via logcat:** UnsatisfiedLinkError: No implementation found for ...SQLiteConnection.nativeOpen � SQLCipher native library libsqlcipher.so was never loaded via System.loadLibrary(). Room's databaseBuilder().build() does NOT open the database; it only creates the object. The actual native call happens lazily on a background thread during the first Room query, so the catch (Throwable) in getInstance() never saw it.
- **Defense in depth:** HushBroDatabase.getInstance() catches Throwable (not just Exception) and falls back to plain Room if SQLCipher fails. providePlainRoomFallback() has its own try-catch and final retry.
- **Files changed:** HushBroApp.kt (added System.loadLibrary), HushBroDatabase.kt (Throwable catch + multi-level fallback)
- **Build verification:** :app:compileDebugKotlin ?, :app:testDebugUnitTest ? (19 tests), :app:assembleDebug ?

- **ROOT CAUSE FOUND via logcat:** UnsatisfiedLinkError - SQLiteConnection.nativeOpen - SQLCipher native library was never loaded via System.loadLibrary. Room build() does not open the database; it only creates the object. The actual native call happens lazily on a background thread during the first Room query, so the catch block in getInstance() never saw it.
- **Fix in HushBroApp.onCreate():** Added System.loadLibrary for sqlcipher wrapped in try-catch, executed before any database operations.
- **Defense in depth:** HushBroDatabase.getInstance() catches Throwable (not just Exception) and falls back to plain Room if SQLCipher fails. providePlainRoomFallback() has its own try-catch and final retry.
- **Files changed:** HushBroApp.kt (added System.loadLibrary), HushBroDatabase.kt (Throwable catch + multi-level fallback)
- **Build verification:** compileDebugKotlin PASSED, testDebugUnitTest PASSED (19 tests), assembleDebug PASSED

- **ROOT CAUSE CONFIRMED via logcat (final):** Two distinct startup crashes:
  2. SQLiteNotADatabaseException (code 26): file is not a database � Existing hushbro.db was incompatible with SQLCipher (plain SQLite or corrupted). Room.databaseBuilder().build() does NOT open the DB; it opens lazily on first query. Fixed by calling context.deleteDatabase() before SQLCipher init so a fresh encrypted DB is created.
  3. catch (e: Exception) does not catch java.lang.Error subclasses (UnsatisfiedLinkError, SQLiteNotADatabaseException extends RuntimeException). Fixed by using catch (e: Throwable) everywhere.
  Files changed: HushBroApp.kt (System.loadLibrary + try-catch channels), HushBroDatabase.kt (deleteDatabase before SQLCipher + Throwable catch + multi-level plain Room fallback)
  Build verification: compileDebugKotlin PASSED, testDebugUnitTest PASSED (19 tests), assembleDebug PASSED

- FINAL FIX: Two startup crashes found via logcat:
  1. UnsatisfiedLinkError: System.loadLibrary sqlcipher was never called. Fixed in HushBroApp.onCreate.
  2. SQLiteNotADatabaseException code 26: existing hushbro.db incompatible with SQLCipher. Fixed by deleteDatabase before SQLCipher init.
  3. catch Exception does not catch Error types. Fixed by catch Throwable everywhere.
  Files: HushBroApp.kt, HushBroDatabase.kt
  Build: compileDebugKotlin PASSED, testDebugUnitTest PASSED 19 tests, assembleDebug PASSED

- **Fixed 3 runtime bugs:**
  1. Database cleared on every app start: Removed context.deleteDatabase() from HushBroDatabase.getInstance() main path. It now only deletes in catch blocks during fallback.
  2. UI not scrollable: Added verticalScroll(rememberScrollState()) to HushBroScreen Column. Correct import for Compose Foundation 1.7.8 is androidx.compose.foundation.verticalScroll / rememberScrollState (not .scroll subpackage).
  3. Notification shade count not instant: Added listenerBridge.updateActiveCount() call in HushBroScreen composable body so it runs on first composition, not just on ON_RESUME.
  Files changed: MainActivity.kt (scroll + count refresh), HushBroDatabase.kt (removed deleteDatabase from main path)
  Build: compileDebugKotlin PASSED, testDebugUnitTest PASSED 19 tests, assembleDebug PASSED



## Navigation & UI (Phase 2)
- Bottom nav with 5 destinations; Home/Chronicle/Rules/Insights/Settings screens wired to repos.
- Build and tests pass.

## Muted notifications persist until manually removed
- NotificationDao.deleteOlderThan now skips MUTED rows so muted items survive retention pruning.
- Added deleteById to NotificationDao and NotificationRepository for manual removal.
- ChronicleScreen detail dialog now has a Remove button.
- NotificationPipeline.settingsStore made nullable with null-safe access; logging calls wrapped in runCatching so unit tests don't crash on unmocked android.util.Log.
- Build: testDebugUnitTest PASSED.

## UI Theme: Autumn/Coffee (current)
- Refined `AutumnTheme.kt` color palette: espresso brown primary, cream background, latte surface, sage green secondary, cinnamon tertiary, burnt umber error. Added detailed color role comments.
- Enhanced `AutumnTypography.kt`: Serif display/headline fonts for a warm editorial feel, SansSerif body for readability. Larger display sizes, refined line heights.
- Updated `AutumnShapes.kt`: More rounded corners (12/20/28dp) for softer, coffee-shop aesthetic.
- Added new components in `AutumnComponents.kt`:
  - `HeroCard` — accent-colored featured card with title/subtitle and trailing content slot
  - `CategoryChip` — category-colored chip (OTP/sage, PROMO/burnt umber, SOCIAL/espresso)
  - `StatusChip` — status-colored chip (muted/held/snoozed/digested/kept)
  - `FeatureCard` — icon + title + description card with optional chevron
  - `StatCard` — metric card with emoji icon, value, label, and detail
  - `AutumnDivider` — subtle outline divider
  - `CoffeeSteam` — decorative steam lines above hero cards
- Updated screens with new components:
  - `HomeScreen` — HeroCard with coffee steam, StatCards with emojis, cleaner spacing
  - `ChronicleScreen` — Search field with magnifying glass, FilterChip row, StatusChip on each notification, EmptyState
  - `RulesScreen` — Primary-colored create button, CategoryChip for actions, styled Switch, elevation on cards
  - `InsightsScreen` — StatCards with emojis, AutumnDivider between sections
  - `SettingsScreen` — AutumnDivider between settings groups
- Build: compileDebugKotlin PASSED, testDebugUnitTest PASSED (19 tests), assembleDebug PASSED.
