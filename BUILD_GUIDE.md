# HushBro — Build Guide

## 1. Project Overview

HushBro is an offline-first, privacy-focused Android notification manager being built for INVICTUS Droidrush 2026.

The app observes notifications on the device, redacts sensitive information, applies user-defined rules, classifies notifications, stores safe notification history locally, and provides features such as search, digest notifications, snoozing, and usage insights.

### Core requirements

- **Offline operation:** No backend, no network calls, and no `INTERNET` permission.
- **Notification capture:** Read the originating package, app label, title, text, timestamp, notification category, key, and visibility.
- **Privacy:** Redact OTPs and sensitive card/account-like values before persistent storage. Apps marked private must have their notification content hidden.
- **Rules:** Support `MUTE`, `MARK_IMPORTANT`, and `ROUTE`. Rules run in priority order, and the first matching enabled rule wins.
- **Classification:** Assign one of four local tags: `OTP`, `PROMO`, `SOCIAL`, or `OTHER`.
- **History:** Provide searchable, app-grouped notification history with read/unread and importance indicators.
- **Digest:** Combine eligible low-priority notifications into scheduled digest notifications.
- **Snooze:** Schedule a reminder using already-redacted notification content.
- **Insights and export:** Show notification statistics and allow redacted CSV export.
- **Reliability:** Support deduplication, background processing, batch database writes, retention cleanup, and listener recovery.
- **Security:** Use Room with SQLCipher encryption and protect the encryption key with Android Keystore in the later security stage.

### Important Android limitation

`ROUTE` means assigning an internal label or bucket inside HushBro. Android does not allow HushBro to move another app's notification into a different system notification channel.

Notification Access must be explicitly granted by the user through Android Settings. Android 13+ also requires the appropriate notification permission for HushBro's own digest and reminder notifications.

## 2. Current Project Baseline

- Project name: `HushBro`
- Package/application ID: `com.invictus.hushbro`
- Language: Kotlin
- UI framework: Jetpack Compose
- UI components: Material 3
- Build scripts: Kotlin DSL (`.gradle.kts`)
- Current app shell: A basic Compose screen that successfully launches
- Git repository: Already created and pushed to GitHub

The existing Gradle configuration has successfully built the starter app. Inspect it before changing anything. Do not replace working configuration or upgrade unrelated dependencies without a specific reason.

The PDF build guide specifies `minSdk = 26`. The current project may differ, so verify the existing configuration and align it with the guide when safe.

Use the stable SDK/toolchain versions supported by the current project. Add dependencies in small groups, syncing and building after each group.

## 3. Intended Package Structure

Use this structure progressively. Do not create every class or add every dependency in the first step.

```text
com.invictus.hushbro/
├── HushBroApp.kt
├── MainActivity.kt
├── di/
├── data/
│   ├── db/
│   ├── repo/
│   └── prefs/
├── service/
├── pipeline/
├── work/
├── notify/
├── ui/
│   ├── theme/
│   ├── nav/
│   ├── onboarding/
│   ├── history/
│   ├── rules/
│   ├── settings/
│   └── insights/
└── util/
```

Responsibilities:

- `di/`: Dependency injection.
- `data/db/`: Room database, entities, DAOs, and converters.
- `data/repo/`: Repositories for notifications, rules, and statistics.
- `data/prefs/`: DataStore-backed settings.
- `service/`: Notification listener and listener bridge.
- `pipeline/`: Redaction, rules, classification, and background notification processing.
- `work/`: Digest, snooze, and pruning workers.
- `notify/`: HushBro's notification channels and notification builders.
- `ui/`: Compose screens and navigation.
- `util/`: Shared utilities such as time handling and CSV export.

Create packages when they become necessary rather than generating empty files for the entire structure.

## 4. Data and Naming Conventions

Notification tags:

- `OTP`
- `PROMO`
- `SOCIAL`
- `OTHER`

Notification statuses:

- `KEPT`
- `MUTED`
- `HELD`
- `DIGESTED`
- `SNOOZED`

Represent these as enums with Room type converters when implementing the database. Avoid scattering status and tag string literals throughout the app.

Raw notification content must remain temporary in memory until it has passed through the privacy pipeline. Never persist or log raw sensitive notification text.

## 5. Implementation Roadmap

Follow the detailed 18-step sequence in `HushBro_Build_Guide.pdf`. The major stages are:

1. **Project setup and app shell:** Verify the Compose app, manifest, package, and launch behavior.
2. **Room database:** Implement entities, DAOs, duplicate-safe operations, and database tests.
3. **Repositories, dependency injection, and channels:** Add repositories, DataStore settings, Hilt, and HushBro-owned notification channels.
4. **Notification access:** Add the listener service, onboarding explanation, Settings link, and access-state checks.
5. **Buffered processing pipeline:** Add a bounded queue, background processing, batch writes, and deduplication.
6. **Privacy redaction:** Mask sensitive content before persistence.
7. **Rule engine:** Implement ordered `MUTE`, `MARK_IMPORTANT`, and `ROUTE` rules.
8. **On-device classification:** Assign `OTP`, `PROMO`, `SOCIAL`, or `OTHER`.
9. **History timeline:** Implement app-grouped history, search, paging, and read/clear actions.
10. **Rule editor:** Add rule creation, editing, enable/disable, deletion, and priority ordering.
11. **Settings:** Implement private-app controls, digest preferences, quiet hours, retention, and privacy information.
12. **Digest:** Schedule and deliver eligible notification digests.
13. **Snooze:** Schedule reminders using redacted content.
14. **Insights:** Calculate notification statistics and display charts.
15. **CSV export:** Export redacted history and statistics.
16. **Encryption:** Integrate SQLCipher and protect the key using Android Keystore.
17. **Reliability and performance:** Test bursts, recovery, deduplication, background work, and lifecycle behavior.
18. **Final verification and submission:** Run the test checklist, check privacy requirements, document the app, and prepare the demo.

Refer to the PDF for the exact implementation details and verification criteria for each stage. Do not invent a different roadmap or skip ahead without a clear reason.

## 6. First Milestone — Step 1

Goal: a clean HushBro Compose app that builds, installs, and opens reliably.

Verify or complete:

- `namespace` and `applicationId` are `com.invictus.hushbro`.
- The minimum SDK is aligned with the PDF guide (`26`) when safe.
- The existing Compose screen displays HushBro.
- The manifest has `android:allowBackup="false"`.
- The app does not declare the `INTERNET` permission and contains no network implementation.
- Existing Gradle configuration remains stable.
- The project builds successfully and the app launches without crashing.

Do not implement the database, notification listener, Hilt, or the other major features during this milestone unless a specific change is required to make the app shell work.

## 7. Verification and Git Workflow

After each milestone:

1. Inspect the relevant files before editing.
2. Implement only the current milestone.
3. Build the project and run available relevant tests.
4. Fix errors introduced by the changes.
5. Summarize the files changed and explain how to verify the result.
6. Update `PROGRESS.md` with completed work, verification results, issues, and the next action.
7. Do not claim a build or test passed unless it was actually run successfully.

Commit each verified milestone to Git. Do not commit broken or unverified work as a completed milestone.

## 8. Privacy Rules

- Never add a backend or network calls.
- Never add the `INTERNET` permission.
- Never log real notification text, OTPs, or other secrets.
- Redact sensitive content before it reaches persistent storage.
- Never use real personal notifications as test fixtures.
- Use synthetic test notifications for development and demos.

## 9. Source of Truth

Use the following sources in this order:

1. `AGENTS.md` for coding-agent behavior and implementation constraints.
2. `BUILD_GUIDE.md` for project requirements and implementation order.
3. `PROGRESS.md` for the current state of the project.
4. `HushBro_Build_Guide.pdf` for detailed step-by-step implementation and verification instructions.

If a file or requirement is missing or contradictory, inspect the repository and explain the discrepancy before making a risky change. Do not silently invent completed work or change the product requirements.