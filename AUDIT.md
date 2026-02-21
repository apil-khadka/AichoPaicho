# Audit Report

Date: 2026-02-21

## Scope
- Build-system migration to AGP new DSL mode.
- Third-party plugin compatibility upgrades.
- Microsoft Clarity integration.
- Screenshot documentation refresh.
- P0 product-quality implementation wave:
- due reminders, recurring templates, sync center, real sync progress, CSV import/export, unsafe compiler flag removal.
- Contact privacy simplification:
- intent-only contact picking without broad contacts permission.

## Changes Completed
1. AGP new DSL migration
- Enabled `android.newDsl=true` in `gradle.properties`.
- Enabled `android.builtInKotlin=true` in `gradle.properties`.
- Removed explicit application of `org.jetbrains.kotlin.android` plugin from root and app build scripts to align with AGP built-in Kotlin flow.
- Upgraded Hilt Gradle plugin from `2.57.1` to `2.59.2` to avoid legacy `BaseExtension`-based integration issues with AGP new DSL.
- Upgraded KSP Gradle plugin from `2.3.2` to `2.3.4` to address built-in Kotlin source-set integration error.

2. Clarity integration
- Added Compose SDK dependency `com.microsoft.clarity:clarity-compose:3.8.1` via version catalog.
- Initialized Clarity in `AichoPaichoApp` with project ID `vkq46hwvk6`.
- Ensured WorkManager is initialized before Clarity startup in app initialization flow.
- Added user consent toggle in Settings for analytics and made Clarity initialization conditional on that preference.

3. Due reminders implementation
- Added `DueReminderWorker` for periodic due-date checks and notifications.
- Added notification channel setup in app startup (`NotificationChannels`).
- Added `POST_NOTIFICATIONS` permission in `AndroidManifest.xml`.
- Added due reminder preference toggle in settings state and ViewModel logic.
- Added runtime notification-permission UX in Settings:
- shows clear requirement text when reminders are enabled but permission is missing.
- supports direct permission request and a shortcut to app notification settings.

4. Recurring templates implementation
- Added Room entity `RecurringTemplate` and DAO/repository plumbing.
- Added `RecurringTemplateWorker` to materialize due templates into `Record` entries.
- Extended add-transaction flow to support recurrence options:
- none/daily/weekly/monthly/custom interval.
- Added database migration `4 -> 5` for recurring templates table and indexes.

5. Sync center and real progress
- Refactored sync pipeline to return structured `SyncReport` with per-entity failure details.
- Added `SyncCenterRepository` + sync state models to persist and expose:
- queued count, success count, failed count, failed item list, stage, progress, last sync.
- Updated `BackgroundSyncWorker` to publish real WorkManager progress (`setProgress`) and sync-center stages.
- Added `SyncCenterScreen` + `SyncCenterViewModel` with retry failed items flow.
- Connected navigation route for sync center and integrated actions from settings.

6. CSV export/import
- Added `CsvTransferService` to export/import:
- `contacts.csv`, `records.csv`, `repayments.csv`.
- Added settings UI actions for CSV export/import with operation status and location.

7. Compiler and settings metadata cleanup
- Removed unsafe Kotlin compiler argument:
- `-XXLanguage:+PropertyParamAnnotationDefaultTargetMode`.
- Replaced unresolved `BuildConfig` usage in `SettingsViewModel` with runtime package metadata via `PackageManager` + `PackageInfoCompat`.

8. Legal/support link updates
- Updated settings About section actions to open live website pages.
- Wired URLs:
- Privacy policy: `https://aichopaicho.nyxigale.dev/en/legal/privacy-policy`
- Terms of service: `https://aichopaicho.nyxigale.dev/en/legal/terms-of-service`
- Support/site: `https://aichopaicho.nyxigale.dev/`

9. Privacy and data-use disclosure clarity
- Expanded onboarding permission copy with explicit contact/cloud-sync data handling statements.
- Added dedicated `Data Use & Privacy` section in Settings with clear disclosure bullets.
- Linked disclosure actions to Privacy Policy and Terms pages from both onboarding and settings.

10. Hide-amount privacy mode
- Added Settings toggle to enable/disable amount masking in UI.
- Added shared amount privacy formatter utilities with preference observer support.
- Applied masking to key amount displays across dashboard, transaction list, contact transaction, and transaction detail screens.

11. Contact picker permission model hardening
- Removed `READ_CONTACTS` from app manifest.
- Switched Add Transaction contact selection to intent-based phone picker (`ACTION_PICK` on `CommonDataKinds.Phone`).
- Updated onboarding navigation to go directly from Welcome to Dashboard (removed mandatory contact-permission step from primary flow).
- Reworked contact detail quick action to open the dialer intent from stored phone number, avoiding device-contact lookup.

12. Global search and status filtering expansion
- Added advanced transaction filters in View Transactions:
- text search (contact/name/phone/note/amount), status chips (open/completed/overdue/all), and resilient amount-range parsing.
- Extended similar search + status filtering to Contact Transactions for consistency across list/detail workflows.
- Kept existing type/date/amount controls and integrated filter state into ViewModel-driven list updates.

13. Insights screen implementation
- Added a new `Insights` flow with ViewModel-driven metrics from existing records/repayments:
- current-month inflow/outflow, overdue outstanding totals, top-contact balances, and a six-month trend series.
- Added dedicated UI screen and navigation route (`INSIGHTS_SCREEN`), with Settings entry point (`Open insights`).
- Reused existing amount-format privacy utilities so hidden-amount mode also applies to insights values.

14. Transaction detail UI refresh
- Redesigned the transaction detail card with stronger visual hierarchy:
- hero summary area, status/type chips, repayment progress bar, and compact amount summary tiles.
- Improved repayment UX cards:
- clearer remaining-amount context in add-repayment form, better error hinting, and cleaner repayment history row styling.
- Replaced remaining hardcoded repayment success messaging with localized string resources.
- Refined transaction-detail interactions:
- moved add-repayment flow to an on-demand overlay sheet and added explicit "person not found in contacts" feedback for View Details actions when local contact linkage is unavailable.

## Files Updated For This Work
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/dev/nyxigale/aichopaicho/AichoPaichoApp.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/MainActivity.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/BackgroundSyncWorker.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/DueReminderWorker.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/RecurringTemplateWorker.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/notification/NotificationChannels.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/database/AppDatabase.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/di/DatabaseModule.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/dao/RecordDao.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/dao/RepaymentDao.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/dao/RecurringTemplateDao.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/entity/RecurringTemplate.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/RecordRepository.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/RepaymentRepository.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/PreferenceRepository.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/SyncRepository.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/SyncCenterRepository.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/RecurringTemplateRepository.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/repository/CsvTransferService.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/sync/SyncModels.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/data/sync/SyncCenterState.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/navigation/Routes.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/navigation/AppNavigationGraph.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/screens/SettingScreen.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/screens/PermissionScreen.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/screens/AddTransactionScreen.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/screens/SyncCenterScreen.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/component/SettingComponent.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/component/DashboardComponent.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/component/ViewTransactionComponent.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/component/ContactTransactionComponent.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/component/TransactionDetailComponent.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/ui/util/AmountPrivacy.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/SettingsViewModel.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/AddTransactionViewModel.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/SyncCenterViewModel.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/data/SettingUiState.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/data/AddTransactionUiState.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/data/AddTransactionUiEvents.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/data/RecurrenceType.kt`
- `app/src/main/java/dev/nyxigale/aichopaicho/viewmodel/data/SyncCenterUiState.kt`
- `app/src/main/res/values/strings.xml`
- `SCREENSHOTS.md`
- `task.md`
- `AUDIT.md`

## Validation Notes
- `BuildConfig` unresolved reference issue in `SettingsViewModel` was fixed by switching to package metadata lookup.
- Full Gradle assemble/test validation for this feature wave could not be executed in the sandbox due wrapper/network restrictions and interrupted escalation.
- Validation should be completed in Android Studio / local host environment:
- run `:app:assembleDebug`, basic navigation smoke tests, and worker-related behavior checks.

## Current Feature Inventory
- User onboarding and welcome flow.
- Contact permission flow.
- Dashboard with summary and due-focused views.
- Contact management (create/view/update/delete).
- Transaction and repayment tracking.
- Transaction detail and per-contact transaction views.
- Settings screen for app preferences.
- Google Sign-In via Firebase Authentication.
- Cloud sync/backup with Firestore.
- Offline-first local persistence via Room.
- Background synchronization via WorkManager.
- Sync center screen with retry failed items support.
- Due reminder notifications (periodic worker + notification channel).
- Recurring transaction template generation.
- CSV export/import for contacts/records/repayments.
- Hilt-based dependency injection.
- Jetpack Compose UI and navigation graph.
- Microsoft Clarity session analytics integration.
