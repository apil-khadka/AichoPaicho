# AichoPaicho - Android Application

AichoPaicho is an Android application designed to help users manage personal records and contacts, with a focus on tracking financial entries or other types of records associated with these contacts. It supports local data storage and synchronization with Firebase (Firestore and Firebase Authentication) for backup and multi-device access.

## Key Features

*   **User Management**:
    *   User registration and login (Google Sign-In via Firebase Authentication).
    *   Offline-first approach with a local user profile.
    *   Online synchronization of user data.
*   **Contact Management**:
    *   Create, view, update, and delete contacts.
    *   Store contact details like name and phone numbers.
    *   Link contacts to a user account.
    *   Contact data is stored locally and may be synced to Firestore when backup/sync is enabled.
*   **Record Tracking**:
    *   Log records/transactions with details such as type, amount, date, description, and completion status.
    *   Associate records with specific contacts.
*   **Data Synchronization**:
    *   Robust sync mechanism with Firestore to backup and restore user data, contacts, and records.
    *   Timestamp-based conflict resolution to merge local and remote data.
    *   Background synchronization using Android WorkManager.
*   **Settings & Configuration**:
    *   User-configurable settings, including currency preferences.
    *   Enable/disable backup and sync.
    *   View app information (version, build number).
*   **Modern Android UI**:
    *   Built with Jetpack Compose for a declarative and modern user interface.
    *   Clean dashboard to display summaries and navigate through app features.

## Screenshots

Please click the link to see the [Screenshots](SCREENSHOTS.md) of app.

## Technology Stack

*   **Programming Language**: Kotlin
*   **UI Framework**: Jetpack Compose
*   **Architecture**: MVVM (Model-View-ViewModel) with a clear separation of concerns (UI, ViewModel, Repository, Data Source).
*   **Dependency Injection**: Hilt (Dagger Hilt)
*   **Local Database**: Room Persistence Library
*   **Backend & Sync**:
    *   Firebase Firestore (for cloud database)
    *   Firebase Authentication (for Google Sign-In)
*   **Asynchronous Programming**: Kotlin Coroutines & Flow
*   **Background Tasks**: Android WorkManager
*   **Build System**: Gradle

## Project Structure

The project is organized into standard Android app modules and follows a typical modern Android architecture:

*   **/app/src/main/java/com/aspiring_creators/aichopaicho/**
    *   **`data/`**: Contains data layer components:
        *   **`dao/`**: Room Data Access Objects (e.g., `UserDao`).
        *   **`entity/`**: Room entity classes (e.g., `User.kt`, `Contact.kt`, `Record.kt`).
        *   **`repository/`**: Repository classes abstracting data sources (e.g., `UserRepository.kt`, `SyncRepository.kt`).
        *   `BackgroundSyncWorker.kt`: Handles background data synchronization.
    *   **`ui/`**: Contains UI layer components (Jetpack Compose):
        *   **`screens/`**: Composable functions for different application screens (e.g., `DashboardScreen.kt`, `SettingsScreen.kt`).
        *   **`component/`**: Reusable UI components.
        *   **`theme/`**: App theme and styling.
    *   **`viewmodel/`**: ViewModel classes (e.g., `DashboardScreenViewModel.kt`, `SettingsViewModel.kt`).
    *   `AichoPaichoApp.kt`: Main Application class.
    *   `CurrencyUtils.kt`: Utility functions.
*   **`/app/src/main/res/`**: Android resources (layouts, drawables, values).
*   **Build Scripts**: `build.gradle.kts` (app and project level), `gradle/libs.versions.toml`.

## Development Setup

Follow these steps to set up the project for development:

### 1. Clone the Repository
Clone this repository to your local machine:
```bash
git clone https://github.com/Aspiring-Creators-Nightingale/AichoPaicho.git
cd AichoPaicho
```

### 2. Open in Android Studio
Open the cloned project in Android Studio. Android Studio will automatically sync the Gradle project.

### 3. Create a Development Branch
Before making any changes, create a new branch for your feature or bug fix:
```bash
git checkout -b your-feature-branch-name
```
Replace `your-feature-branch-name` with a descriptive name for your branch (e.g., `feature/add-new-setting` or `fix/login-bug`).

### 4. Firebase Setup (for Online Functionality)
To use online synchronization features (Firebase Firestore and Authentication), you'll need to set up a Firebase project. If you are working on UI or offline features, you can skip this step, but Google Sign-In and cloud sync will not be available.

*   **Create a Firebase Project**: Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
*   **Add an Android App**: Within your Firebase project, add a new Android app.
    *   Use `dev.nyxigale.aichopaicho` as the package name.
    *   You can skip the SHA-1 certificate hash for now if you are just testing, or add it if you intend to use Google Sign-In.
*   **Download `google-services.json`**: After registering the app, download the `google-services.json` file.
*   **Place `google-services.json`**: Copy the downloaded `google-services.json` file into the `app/` directory of your Android Studio project (e.g., `AichoPaicho/app/google-services.json`).
*   **Enable Firebase Services**:
    *   **Authentication**: In the Firebase console, go to "Authentication" and enable the "Google" sign-in provider. You will need to provide your app's SHA-1 fingerprint for this.
    *   **Firestore**: In the Firebase console, go to "Firestore Database" and create a database. Start in test mode for easier setup, but remember to secure your rules before production.
*   **Update Web Client ID for Google Sign-In**:
    *   The Google Sign-In process requires a web client ID. This ID is automatically generated by Firebase when you enable Google Sign-In.
    *   Open the `strings.xml` file located at `app/src/main/res/values/strings.xml`.
    *   Update the value of the `web_client` string with the Web client ID from your Firebase project's Google Sign-In configuration. It usually looks something like `YOUR_WEB_CLIENT_ID.apps.googleusercontent.com`.
    ```xml
    <string name="web_client">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
    ```

### 5. Make Your Changes and Commit
Implement your features or bug fixes. Commit your changes regularly with clear and concise commit messages.

### 6. Push Your Branch and Create a Pull Request
Once your changes are complete and tested, push your branch to the remote repository:
```bash
git push origin your-feature-branch-name
```
After pushing, go to the repository on the Git hosting platform (e.g., GitHub, GitLab) and create a pull request (PR) from your branch to the main development branch (e.g., `main` or `develop`). Wait for a code review and address any feedback.

### 7. Build and Run
You should now be able to build and run the application on an Android emulator or a physical device.

*   **Offline Use**: If you skipped the Firebase setup, the app will still function using the local Room database, but cloud synchronization and Google Sign-In will not be available.
*   **Online Use**: With Firebase configured, the app will attempt to sync data with Firestore and allow users to sign in with their Google accounts.

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
