# AGENTS.md — KharjNegar (خرج‌نگار)

## Overview

**KharjNegar** (`com.kharjnegar.app`) is a 100% offline, open-source Android expense tracker that parses Iranian bank SMS messages (25+ banks) strictly on-device. It captures incoming and historical SMS, filters OTP/noise messages, normalizes Rial/Toman amounts, auto-categorizes transactions, renders Jalali (Solar Hijri) monthly analytics, and supports local SAF CSV/JSON backup/restore. Zero network permissions are declared or allowed.

## Tech Stack & Requirements

- **Language**: Kotlin `2.0.21` (JVM target 17)
- **UI**: Jetpack Compose (`compose-bom:2024.12.01`), Material 3, Material Icons Extended, offline Vazirmatn fonts (`app/src/main/res/font/`)
- **Persistence**: Room `2.6.1` (with KSP `2.0.21-1.0.27`), Jetpack DataStore Preferences `1.1.1`
- **Concurrency**: `kotlinx-coroutines-android:1.9.0`
- **Build**: Android Gradle Plugin `8.7.3`, Gradle `8.11.1+` (no `gradlew` wrapper script in repo; invoke `gradle` directly), JDK 17, Android SDK `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- **Tests**: JUnit 4 (`4.13.2`), `org.json:20240303`, `kotlinx-coroutines-test:1.9.0`

## Repository Structure

```text
kharjnegar/
├── build.gradle.kts                       # Root plugin declarations (AGP, Kotlin, Compose, KSP)
├── settings.gradle.kts                    # Single module (:app)
├── docs/                                  # Static GitHub Pages site & live SMS parser demo
├── .github/workflows/
│   ├── ci.yml                             # Unit tests, debug APK, aapt2 offline permission audit
│   └── release.yml                        # Tagged v* release APK + SHA-256 + GitHub Release
└── app/
    ├── build.gradle.kts                   # App config, R8 minification, env-based release signing
    ├── proguard-rules.pro                 # R8 keep rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # READ_SMS, RECEIVE_SMS, POST_NOTIFICATIONS; allowBackup=false
        │   ├── java/com/kharjnegar/app/
        │   │   ├── KharjNegarApp.kt       # Application class: manual DI (Room DB, Repo, DataStore, Scanner)
        │   │   ├── MainActivity.kt        # Single-activity Compose host, permissions, SAF CSV/JSON launchers
        │   │   ├── data/                  # KharjNegarDatabase, TransactionEntity, TransactionDao,
        │   │   │                          # TransactionRepository, UserPreferences, BackupManager
        │   │   ├── domain/                # IranianBank (25+ banks), JalaliCalendar, PersianNormalizer, Models
        │   │   ├── parser/                # BankSmsParser, TransactionCategorizer
        │   │   ├── sms/                   # SmsReceiver (BroadcastReceiver), SmsInboxScanner, NotificationHelper
        │   │   └── ui/
        │   │       ├── MainViewModel.kt   # StateFlow UI state, filtering, SMS scan, sample data, export/import
        │   │       ├── Strings.kt         # Bilingual FA (RTL) / EN (LTR) strings
        │   │       ├── screens/           # DashboardScreen, TransactionsScreen, AnalyticsScreen, SettingsScreen
        │   │       ├── components/        # Charts, Dialogs, CategoryIcons
        │   │       └── theme/Theme.kt     # Material 3 Light/Dark + Vazirmatn typography + RTL/LTR layout
        │   └── res/                       # Fonts, mipmaps, themes
        └── test/java/com/kharjnegar/app/  # Unit tests: BankSmsParserTest, JalaliCalendarTest,
                                           # PersianNormalizerTest, TransactionCategorizerTest, BackupManagerTest
```

## Architecture & Key Flows

- **Entry Points**: `KharjNegarApp` initializes lazy singletons (`KharjNegarDatabase`, `TransactionRepository`, `UserPreferences`, `SmsInboxScanner`) and creates the notification channel. `MainActivity` hosts the 4-tab Material 3 Compose `Scaffold` (Dashboard, Transactions, Analytics, Settings) and owns runtime SMS permission and Storage Access Framework (`CreateDocument` / `OpenDocument`) launchers.
- **Real-Time SMS Capture**: `SmsReceiver` (`Telephony.SMS_RECEIVED`, priority `999`) checks `autoCapture` preference, parses the SMS via `BankSmsParser`, categorizes via `TransactionCategorizer`, inserts into Room with SHA-256 deduplication, and optionally notifies via `NotificationHelper`.
- **Inbox Scan & Tester**: `SmsInboxScanner` queries `content://sms/inbox` read-only over a configurable lookback window (30d / 90d / 1y / All). Users can also test/import pasted SMS via `SmsTesterDialog` or load demo data without granting SMS permissions.
- **Currency & Calendar**: All amounts are normalized internally to **Rial** (`Long`) regardless of whether an Iranian bank SMS reports Rial or Toman (e.g., Blu Bank defaults to Toman) and formatted for display in the user's preferred unit. Dates and monthly charts use the pure-Kotlin `JalaliCalendar`.
- **Backup & Export**: `BackupManager` produces UTF-8 BOM CSV (Excel-friendly Persian) and JSON backup/restore payloads written/read via Android SAF.

## Development & Commands

```bash
# Run all JVM unit tests (parser, Jalali calendar, normalizer, categorizer, backup)
gradle testDebugUnitTest --stacktrace

# Build debug APK (applicationIdSuffix = ".debug")
gradle assembleDebug --stacktrace

# Build R8-minified release APK (falls back to debug signing if KEYSTORE_FILE is unset)
gradle assembleRelease --stacktrace

# Verify 100% offline guarantee (mirrors CI audit)
aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk
```

## Configuration & Environment

- **Release signing** (optional env vars read in `app/build.gradle.kts`):
  - `KEYSTORE_FILE` — path to `.jks`/`.keystore` file
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
- **No `.env` files, API keys, or backend URLs** exist — the app is strictly offline.

## Conventions & Gotchas

- **Zero-Internet Invariant (CI-Enforced)**: Never add `android.permission.INTERNET` or `android.permission.ACCESS_NETWORK_STATE`, and never add a dependency that merges them into the manifest. Both `ci.yml` and `release.yml` run `aapt2 dump permissions` and fail the build if any network permission appears.
- **`android:allowBackup="false"`**: Keep cloud backup disabled in `AndroidManifest.xml` to protect local financial data.
- **No `gradlew` script**: The repo contains `gradle/wrapper/gradle-wrapper.properties` but not `gradlew`/`gradlew.bat`; use a system-installed Gradle `8.11.1+`.
- **Adding bank support**: Register bank metadata/sender patterns in `domain/IranianBank.kt`, handle format quirks in `parser/BankSmsParser.kt` (normalizing Persian/Arabic digits via `PersianNormalizer`), and add redacted test fixtures to `BankSmsParserTest.kt`.
- **Manual DI**: No Hilt/Dagger/Koin — wire new dependencies through `KharjNegarApp` and `MainViewModel.factory(...)`.
