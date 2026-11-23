# خرج‌نگار — KharjNegar

[![CI Build & Offline Privacy Audit](https://github.com/HamidRezaAshkiyan/kharjnegar/actions/workflows/ci.yml/badge.svg)](https://github.com/HamidRezaAshkiyan/kharjnegar/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/HamidRezaAshkiyan/kharjnegar)](https://github.com/HamidRezaAshkiyan/kharjnegar/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-emerald.svg)](LICENSE)
[![100% Offline](https://img.shields.io/badge/Privacy-100%25%20Offline%20%7C%20Zero%20Internet%20Permission-059669.svg)](#privacy-guarantee)

**100% Offline, Open-Source Iranian Bank SMS Expense Tracker for Android**  
**ردیاب هزینه‌های بانکی از روی پیامک — ۱۰۰٪ آفلاین و متن‌باز برای اندروید**

---

## Privacy Guarantee

KharjNegar is engineered so that **your financial data can never leave your phone**:

- **Zero `INTERNET` permission** — `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE` are intentionally **not declared** in `AndroidManifest.xml`.
- **Zero Cloud Backup** — `android:allowBackup="false"` prevents Android from syncing your local SQLite database to cloud servers.
- **Zero Analytics / Telemetry / Trackers** — no Firebase, no Crashlytics, no third-party SDKs.
- **Automated CI Audit** — every GitHub Actions build runs `aapt2 dump permissions` against the compiled APK and fails if any network permission is ever introduced.

You can verify any release APK yourself:

```bash
aapt2 dump permissions kharjnegar-v1.0.0.apk
```

Only `READ_SMS`, `RECEIVE_SMS`, and optional `POST_NOTIFICATIONS` (Android 13+) are declared.

---

## Features

- **Real-Time & Historical SMS Parsing**: Automatically captures incoming Iranian bank SMS in the background and lets you scan your existing inbox (30 days, 90 days, 1 year, or all messages) with SHA-256 deduplication.
- **Smart OTP & Noise Filtering**: Ignores dynamic passwords (`رمز پویا`), verification codes (`کد تایید`), failed transactions (`ناموفق`), and promotional messages.
- **Rial & Toman Normalization**: Automatically detects whether each bank SMS uses `ریال` (e.g., Mellat, Melli, Saman) or `تومان` (e.g., Blu Bank), normalizes internally, and displays everything in your preferred unit (Toman or Rial).
- **Solar Hijri (Jalali / Shamsi) Calendar**: Pure Kotlin Jalali calendar engine with monthly navigation (`فروردین` .. `اسفند`), daily spending bar charts, and 6-month income vs. expense comparisons.
- **Multi-Bank Balance Overview**: Tracks the latest balance reported by each bank/account automatically.
- **Auto-Categorization**: Built-in rules for Iranian merchants & services (Snapp, SnappFood, Digikala, Ofogh Kourosh, Irancell, Hamrah-e Aval, pharmacies, utility bills, transfers, bank fees, salary) with manual editing.
- **Built-In SMS Parser Tester**: Paste any bank SMS inside the app to test parsing before importing.
- **Bilingual RTL/LTR + Vazirmatn Font**: Native Persian (`فارسی`) RTL and English LTR toggle with offline bundled Vazirmatn typography and Material 3 Light/Dark themes.
- **Local Export & Backup**:
  - **CSV Export** with UTF-8 BOM (opens cleanly in Microsoft Excel & LibreOffice with Persian text intact)
  - **JSON Backup & Restore** via Android Storage Access Framework

---

## Supported Iranian Banks (25+)

| Bank | فارسی | Default SMS Unit |
| :--- | :--- | :--- |
| Bank Mellat | بانک ملت | Rial |
| Bank Melli Iran | بانک ملی ایران | Rial |
| Blu Bank | بلوبانک | Toman |
| Saman Bank | بانک سامان | Rial |
| Bank Saderat | بانک صادرات | Rial |
| Bank Tejarat | بانک تجارت | Rial |
| Bank Sepah | بانک سپه | Rial |
| Bank Pasargad | بانک پاسارگاد | Rial |
| Wepod | ویپاد | Rial / Toman |
| Bank Keshavarzi | بانک کشاورزی | Rial |
| Refah Bank | بانک رفاه | Rial |
| Bank Maskan | بانک مسکن | Rial |
| Parsian Bank | بانک پارسیان | Rial |
| Shahr Bank | بانک شهر | Rial |
| Ayandeh Bank | بانک آینده | Rial |
| Resalat Bank | بانک رسالت | Rial |
| Mehr Iran Bank | بانک مهر ایران | Rial |
| Sina Bank | بانک سینا | Rial |
| Eghtesad Novin | بانک اقتصاد نوین | Rial |
| Karafarin Bank | بانک کارآفرین | Rial |
| Bank Dey | بانک دی | Rial |
| Tourism Bank | بانک گردشگری | Rial |
| Sarmayeh Bank | بانک سرمایه | Rial |
| Iran Zamin Bank | بانک ایران زمین | Rial |
| Post Bank Iran | پست بانک ایران | Rial |
| Generic Fallback | سایر بانک‌ها | Auto-detected |

---

## Installation

Download the latest APK directly from the [GitHub Releases](https://github.com/HamidRezaAshkiyan/kharjnegar/releases) page and install it on Android 8.0+ (API 26+).

---

## Building from Source

Requirements: **JDK 17**, **Android SDK (API 35)**, **Gradle 8.11.1+**.

```bash
git clone https://github.com/HamidRezaAshkiyan/kharjnegar.git
cd kharjnegar

# Run unit tests (SMS parser, Jalali calendar, categorizer, backup/export)
gradle testDebugUnitTest

# Build Release APK
gradle assembleRelease
```

---

## Tech Stack

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material 3 + Custom Canvas Charts + Bundled Vazirmatn Font
- **Local Storage**: Room (SQLite) + Jetpack DataStore Preferences
- **Architecture**: Single-Activity MVVM with `StateFlow` & Coroutines

---

## License

MIT License — see [LICENSE](LICENSE).
