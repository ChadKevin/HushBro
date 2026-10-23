# AGENTS.md — HushBro Development Instructions

## 1. Project Overview

HushBro is an offline-first Android notification management application being developed for the INVICTUS Droidrush 2026 hackathon.

The app helps users manage notifications through configurable rules, local categorization, notification history, scheduled digests, snoozing, and insights.

The primary goal is to implement the hackathon requirements correctly, incrementally, and with a reliable, testable codebase.

## 2. Existing Project Configuration

- Application name: HushBro
- Package name: `com.invictus.hushbro`
- Language: Kotlin
- UI framework: Jetpack Compose
- UI components: Material 3
- Current compile SDK: 36
- Current minimum SDK: 24
- Current target SDK: 36
- Current Android Gradle Plugin: 8.11.2
- Current Kotlin version: 2.0.21

The basic application currently builds and runs successfully.

IMPORTANT:
- Preserve the existing working Gradle configuration.
- Do not upgrade Android Gradle Plugin, Kotlin, SDK versions, or dependencies without a concrete technical reason.
- Check dependency compatibility before introducing a new library.
- Do not replace the working project with a newly generated project.
- Do not modify the application ID or package name.
- Keep the existing launcher activity and Android manifest configuration working.

## 3. Development Rules

1. Implement one feature at a time.
2. Before editing files, inspect the existing implementation and relevant project configuration.
3. Prefer small, understandable changes over large rewrites.
4. Explain which files are created or modified and why.
5. Do not introduce unnecessary dependencies.
6. Do not leave placeholder implementations for required functionality.
7. Handle exceptions and Android lifecycle changes appropriately.
8. Use Kotlin coroutines and Flow for asynchronous operations where appropriate.
9. Use Jetpack Compose for application screens.
10. Keep business logic separate from UI code.
11. Avoid deprecated APIs when a suitable supported alternative exists.
12. Never claim a build or test succeeded unless it was actually run successfully.
13. After each meaningful implementation step, provide the exact build or test command and manual verification steps.
14. Do not implement future phases prematurely.

## 4. Required Application Architecture

Organize application code under:

`app/src/main/java/com/invictus/hushbro/`

Use the following packages as the implementation grows:

- `di/` — dependency injection
- `data/db/` — database, entities, DAOs, migrations
- `data/repo/` — repositories
- `data/prefs/` — persistent preferences
- `service/` — Android notification listener service
- `pipeline/` — notification processing and classification
- `work/` — scheduled background jobs
- `notify/` — digest and notification presentation
- `ui/nav/` — navigation
- `ui/onboarding/` — onboarding and permission guidance
- `ui/history/` — notification history
- `ui/rules/` — notification rules
- `ui/settings/` — application settings
- `ui/insights/` — analytics and export
- `util/` — shared utilities

Do not create empty packages unnecessarily. Add each package when its first feature is implemented.

Keep `MainActivity.kt` and the generated Compose theme files unless a specific change is necessary.

## 5. Core Functional Requirements

Implement the following requirements from the hackathon specification.

### Onboarding and Permissions
- Explain the app's purpose and offline-first behavior.
- Guide the user to enable Notification Access in Android settings.
- Request `POST_NOTIFICATIONS` on Android 13 and newer when required for HushBro's own notifications.
- Clearly handle permission denial and revoked access.

### Notification Capture
- Implement `NotificationListenerService`.
- Extract relevant notification metadata, including package name, title, text, timestamp, category, and notification key when available.
- Handle removed notifications and service lifecycle events safely.
- Avoid retaining sensitive content unnecessarily.
- Redact sensitive information before persisting notification data.

### Rule Processing
- Support first-match rule evaluation.
- Support the actions MUTE, MARK_IMPORTANT, and ROUTE_TO_CHANNEL.
- Keep rule evaluation deterministic and independently testable.

### Local Categorization
- Support the categories OTP, PROMO, SOCIAL, and OTHER.
- Perform categorization on-device.
- Do not upload notification contents to external services.

### Local Storage
- Use Room for structured persistence.
- Use SQLCipher for database encryption when implementing the database layer.
- Protect encryption keys using Android Keystore-backed key protection.
- Add appropriate indexes, migrations, and bounded retention.
- Never commit real notification data, database files, or encryption keys.

### Background Processing
- Use WorkManager for scheduled digest generation, snoozing, and pruning.
- Ensure jobs are safe to retry.
- Batch database writes where appropriate.

### History and Insights
- Provide searchable, app-grouped notification history.
- Implement digest and snooze workflows.
- Support insights and CSV export.
- Keep all user data on-device.

## 6. Privacy and Security Requirements

- Do not add the INTERNET permission.
- Do not send notification content, metadata, or analytics to external servers.
- Redact OTPs, payment-card information, account information, and sensitive content before persistence.
- Apply privacy controls before logging or storing notification text.
- Do not log sensitive notification content in production.
- Do not store secrets or real user notification data in the Git repository.
- Keep permissions limited to those required by implemented features.
- Verify the final merged Android manifest.

## 7. Testing Requirements

- Add unit tests for rule evaluation, categorization, redaction, and retention.
- Test notification processing with synthetic notification fixtures.
- Test database behavior and migrations.
- Test permission-denied and permission-revoked scenarios.
- Verify that digest and snooze jobs behave correctly.
- Never use actual personal notifications as test data.
- Run the relevant tests and build before declaring a feature complete.

## 8. Git and Repository Rules

- Keep generated build outputs and local machine configuration out of Git.
- Never commit `local.properties`, signing credentials, private keystores, encryption keys, or real databases.
- Make focused commits with descriptive messages.
- Do not discard existing user changes.
- Do not rewrite Git history unless explicitly requested.

## 9. Implementation Workflow

For every feature:

1. Explain the goal and relevant requirements.
2. Identify the files to create or modify.
3. Implement the smallest complete version.
4. Explain how the implementation works.
5. Run or provide the appropriate build and test commands.
6. Give manual verification steps.
7. Update `PROGRESS.md` with completed work, tests, and remaining issues.
8. Stop and resolve build errors before starting the next feature.

## 10. First Milestone

The first milestone is to establish the app's basic structure and navigation while preserving the existing working application.

Do not implement notification capture, database encryption, background jobs, or other advanced features until their respective milestones are reached.

The final application must satisfy the hackathon specification, remain offline-first, protect sensitive notification data, and build reliably.