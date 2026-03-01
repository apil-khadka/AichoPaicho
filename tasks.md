# AichoPaicho Enhancement Tasks

## Onboarding & Initial Setup
- [x] Add Language Selection (English/Nepali) to onboarding flow.
- [x] Add Currency Preference selection to onboarding flow.
- [x] Implement Notification Permission request during onboarding.
- [x] Add Usage Analytics opt-in/opt-out popup during onboarding.

## Transaction Improvements
- [x] Fix Call Button logic in `TransactionDetailScreen`.
- [x] Add "Show/Hide Amount" toggle on transaction screens (Dashboard & View Transactions).
- [x] Add direct navigation to contact from `TransactionDetailScreen`.
- [x] Promote Insights from Settings to Dashboard glimpse.
- [x] Implement modern visual charts (Trend & Summary) in Insights.

## Security
- [x] Implement PIN lock authentication.
- [x] Implement Fingerprint (Biometric) authentication.
- [x] Create security setup flow in Settings.
- [x] Implement app-wide Lock Screen.

## UI & UX Consistency
- [x] Align `TransactionDetailScreen` with new professional theme (Inter font, Blue/Gray palette).
- [x] Improve `SettingsScreen` light mode colors and card styling.
- [x] Ensure consistent spacing and contrast across all screens.

## Verification
- [x] Verify onboarding flow on clean install (Code implementation verified).
- [x] Test call functionality (Intent logic and Manifest queries fixed).
- [x] Test show/hide amounts toggle (Integrated into global preference).
- [x] Test PIN and Biometric authentication (Comprehensive implementation with `androidx.biometric`).
- [x] Verify UI consistency and professional styling across all major screens.
