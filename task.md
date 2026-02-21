# Task Backlog

Date: 2026-02-21

## Recently Completed
- AGP new DSL migration for build compatibility.
- Enabled `android.newDsl=true` and `android.builtInKotlin=true`.
- Updated Gradle plugins for compatibility:
- `com.google.dagger.hilt.android` -> `2.59.2`
- `com.google.devtools.ksp` -> `2.3.4`
- Integrated Microsoft Clarity with project ID `vkq46hwvk6`.
- Updated `SCREENSHOTS.md` with new images from `Pic/`.
- Updated `AUDIT.md` with migration + feature inventory.
- Added due reminders with notification channel + scheduled worker + settings toggle.
- Added recurring transaction templates (daily/weekly/monthly/custom) and recurring generation worker.
- Added sync center flow with queued/success/failed counts, last sync and retry failed items.
- Replaced fake sync progress with real WorkManager progress state.
- Added CSV export/import support for contacts, records and repayments.
- Removed unsafe Kotlin compiler flag `-XXLanguage:+PropertyParamAnnotationDefaultTargetMode`.
- Fixed settings version/build metadata loading by using `PackageManager` instead of `BuildConfig`.
- Wired legal/support links in settings About section to live website URLs:
- Privacy: `https://aichopaicho.nyxigale.dev/legal/privacy`
- Terms: `https://aichopaicho.nyxigale.dev/legal/terms`
- Website/Support: `https://aichopaicho.nyxigale.dev/`
- Updated CSV flow to use user-selected SAF destinations:
- export now prompts for folder, import now prompts for file.
- Refactored Add Transaction screen for cleaner spacing and reduced visual clutter.
- Added stronger Add Transaction validation for empty/invalid input before save.
- Added undo snackbars for transaction complete/delete actions in list screens.
- Added analytics consent toggle in Settings for Microsoft Clarity initialization.
- Added clearer privacy/data-use disclosure in Permission and Settings screens for contacts + cloud sync.
- Added runtime notification permission UX in Settings for due reminders (request + app notification settings shortcut).
- Added optional hide-amount mode with Settings toggle and masked amounts across dashboard/transaction screens.

## Project Analysis Snapshot
- Strengths:
- Solid modern Android stack (Compose, Hilt, Room, WorkManager, Firebase Auth/Firestore).
- Offline-first architecture with sync support.
- Clear MVVM and repository structure.
- Current gaps from codebase inspection:
- Testing is minimal (only example tests in `app/src/test` and `app/src/androidTest`).
- Runtime notification permission UX is still basic (worker checks permission, but no dedicated prompt flow in settings).
- CSV currently operates on app-managed storage bundle path (no SAF picker/share/export destination chooser yet).
- Sync reliability still needs queue-table/batch/idempotency hardening.
- `SharedPreferences` still used for settings (could migrate to DataStore).
- Full end-to-end build + device QA is still pending after latest feature wave.

## Priority Roadmap

### P0 (High Impact, Build Product Quality)
- [x] Add due reminders with notification channels.
- [x] Add recurring transaction templates (daily/weekly/monthly/custom).
- [x] Add sync center screen:
- queued/failed/success counts, last sync, retry failed items.
- [x] Replace fake sync progress UI with real WorkManager progress updates.
- [x] Add CSV export/import (contacts, records, repayments).
- [x] Remove or replace unsafe compiler flag after compatibility check.
- [ ] P0 stabilization pass:
- runtime notification permission prompt UX, on-device QA, and edge-case fixes.
- [ ] UX stabilization follow-up:
- verify add-transaction layout on small screens, recurring toggle UX, and undo flows after device QA.
- [ ] Responsive layout pass (in progress):
- improve portrait + landscape behavior for core screens (Add Transaction, View Transactions, Transaction Detail) across phone/tablet widths.

### P1 (User Value + Retention)
- [ ] Add analytics/insights screen:
- monthly inflow/outflow, overdue totals, top contacts, trend graph.
- [ ] Add global search and advanced filters:
- by contact, amount range, date range, status, type.
- [ ] Add quick actions on dashboard:
- add transaction, mark repaid, contact shortcuts.
- [ ] Add backup restore wizard and conflict-resolution UI for merge cases.

### P1 (Reliability + Data Integrity)
- [ ] Move sync writes to Firestore batch writes/transactions where suitable.
- [ ] Introduce local sync queue table with per-entity operation tracking.
- [ ] Add idempotency guards and better tombstone handling for deletes.
- [ ] Add network/offline state-aware sync scheduling policy.
- [ ] Add structured logging for sync failures (instead of `println`).

### P2 (Security + Privacy)
- [ ] Add biometric/PIN lock for app open.
- [x] Add optional hide-amount mode in UI.
- [ ] Encrypt sensitive local preferences or move to encrypted storage.
- [x] Add consent/settings for analytics tracking (Clarity toggle).
- [x] Add clearer privacy/data-use disclosure for contacts + cloud sync.

### P2 (Developer Experience)
- [ ] Add unit tests for repositories/viewmodels.
- [ ] Add integration tests for Room + sync merge flows.
- [ ] Add Compose UI tests for core flows:
- onboarding, add transaction, detail, settings.
- [ ] Add CI pipeline:
- lint, test, assemble debug on pull requests.
- [ ] Enable Room schema export and migration tests.
- [ ] Add static analysis setup (detekt/ktlint baseline and gradual enforcement).

## Feature Expansion Ideas
- [ ] Home widget for due today / upcoming totals.
- [ ] Multi-currency per transaction with conversion to base currency.
- [ ] Attach receipt photo/files to transaction.
- [ ] PDF report generation (monthly summary).
- [ ] Contact-level statement export.
- [ ] Undo snackbar for destructive actions.
- [ ] Soft-delete trash bin with restore window.
- [ ] Deep links to open specific contact or transaction.
- [ ] Accessibility pass:
- larger text handling, TalkBack labels, color contrast review.
- [ ] Localization expansion beyond English/Nepali.

## Suggested Execution Order (Pragmatic)
1. P0 stabilization pass (permission UX + device QA + bugfixes).
2. P1 insights + search/filter.
3. Reliability hardening (queue + batch + merge handling).
4. Security/privacy + CI/test scale-up.
