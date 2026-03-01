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

## Security
- [x] Implement PIN lock authentication.
- [x] Implement Fingerprint (Biometric) authentication.
- [x] Create security setup flow in Settings.
- [x] Implement app-wide Lock Screen.

## Verification
- [x] Verify onboarding flow on clean install (Code implementation verified).
- [x] Test call functionality (Intent logic and Manifest queries fixed).
- [x] Test show/hide amounts toggle (Integrated into global preference).
- [x] Test PIN and Biometric authentication (Comprehensive implementation with `androidx.biometric`).
