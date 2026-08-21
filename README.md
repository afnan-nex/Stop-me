# Stop me ⏱️

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026%2B%29-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-FF6F00)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Stop me** is a minimal, friction-driven Android digital-wellbeing application engineered to curb compulsive social media scrolling.

The core principle is simple and unyielding:

> **Every selected app gets a maximum of 30 minutes of usage per calendar day.**

Once an app reaches its 30-minute allowance, **Stop me** immediately blocks access with an on-screen overlay and sends you to your Home screen. Reopening the app immediately triggers the block again.

---

## 🌟 Key Features

### ⏱️ Strict 30-Minute Daily Allowance
- **Per-Package Isolation**: Every protected app (e.g. Instagram, TikTok, YouTube) has its own independent 30-minute timer.
- **Monotonic Timing Engine**: Measured using monotonic elapsed time (`SystemClock.elapsedRealtime()`) rather than wall clock time, making it tamper-proof against device clock changes.
- **Midnight Reset**: Budgets automatically reset to a full 30 minutes at local calendar midnight.

### 🛡️ Real-Time Foreground Monitoring
- **Accessibility Layer**: Instantaneous detection when a protected package enters the foreground.
- **Smart Overlay & IME Filtering**: Distinguishes between the actual target app and transient system overlays (keyboards, volume panels, notification shade) to prevent timer dropouts.
- **Screen & Lock-Aware**: Active tracking pauses immediately when the device screen turns off or locks, and resumes only when unlocked.

### 🔔 Silent Persistent Usage Notification
- While a protected app is in use, a silent, non-intrusive notification displays real-time progress:
  ```
  Instagram
  Minutes used (10:24)
  ```
- Automatically resets to idle monitoring once you leave the app. No sounds, no vibrations, and no popup spam.

### ⚠️ Pre-Expiry Warning Banner
- Displays an animated warning banner across the top of the screen at **T-30 seconds**.
- Configurable visual styles: **Pill**, **Minimal**, or **Bold**.

### 🔒 Two-Phase Unlock Challenge
When an app is blocked, bypassing the lock requires completing a conscious friction challenge:
1. **Phase 1 (150 Taps)**: Tap the circular indicator 150 times with progressive haptic feedback.
2. **Phase 2 (Pledge Verification Dialog)**: Type a randomized mindful pledge paragraph.
   - **Live Word Checker**: Compares typed words against the target pledge in real time.
   - **In-Progress Matching**: Allows prefix typing without premature errors.
   - **Precise Feedback**: Highlights exact word mismatches in red and shows live word count progress.
3. **Targeted +30 Min Grant**: Completing the challenge awards **+30 minutes exclusively to the unlocked app**. All other apps remain blocked.

### ⚙️ App Management & Quick Actions
- **Installed App Icons**: Dynamically loads real application icons from the device package manager.
- **Long-Press Countdown Reset**: Long-pressing any app in the Apps tab opens a quick dialog to reset today's usage back to full allowance.
- **Settings Protected Apps Manager**: Dedicated "Apps" popup under the Data section in Settings to safely remove protected apps via the pledge challenge.
- **Local JSON Backup & Restore**: Export and import your protected app list and usage history without cloud dependencies.

### 📊 Clean Usage Charts
- Daily and 7-day usage analytics rendered with custom Jetpack Compose Canvas charts.

---

## 🏗️ Architecture & Technology Stack

**Stop me** follows modern Android development best practices and Clean Architecture principles:

```
app/
├── core/
│   ├── common/         # Time/Date utilities, InstalledAppsHelper, Extensions
│   ├── designsystem/   # Material 3 themes, typography, shapes, and color tokens
│   └── ui/             # Reusable UI components (AppIconView, etc.)
├── data/
│   ├── backup/         # JSON backup export and import manager
│   ├── local/          # Room DB (Entities, TypeConverters, DAOs)
│   └── repository/     # Repository implementations (Protected apps, Usage, Settings)
├── domain/
│   ├── model/          # Pure domain models (App, DailyUsage, AppSettings, etc.)
│   ├── repository/     # Domain repository interfaces
│   └── usecase/        # Granular use cases (UnlockAppUseCase, ValidateChallengeTextUseCase, etc.)
├── feature/
│   ├── apps/           # Apps list, AppSelectorSheet, usage rows
│   ├── charts/         # Custom Canvas bar charts and usage analytics
│   ├── onboarding/     # Permission setup checklist
│   ├── overlay/        # Warning banner, Blocking overlay, WritingChallengeDialog
│   └── settings/       # Appearance, Warnings, Unlock, Data management
└── service/
    ├── accessibility/  # StopMeAccessibilityService window listener
    ├── notification/   # Silent UsageNotificationManager
    ├── overlay/        # WindowManager overlay coordinator
    └── tracker/        # ForegroundAppTracker monotonic timing state machine
```

| Component | Library / Framework |
| :--- | :--- |
| **Language** | Kotlin 2.1.0 (JVM 17) |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Dependency Injection** | Dagger Hilt 2.51.1 |
| **Persistence** | Room 2.6.1 + Jetpack DataStore Preferences |
| **Concurrency** | Kotlin Coroutines & Reactive Flows |
| **Minimum SDK** | Android 8.0 (API level 26) |
| **Target SDK** | Android 14+ (API level 36) |

---

## 🔒 Permissions & Privacy

**Stop me is 100% offline.** It contains zero trackers, zero analytics, and makes no network requests.

| Permission | Purpose |
| :--- | :--- |
| `SYSTEM_ALERT_WINDOW` | Displays the warning countdown banner and full-screen blocking overlay over other apps. |
| `BIND_ACCESSIBILITY_SERVICE` | Detects when protected apps enter/exit the foreground in real time. |
| `PACKAGE_USAGE_STATS` | Secondary usage reconciliation and historical stats verification. |
| `POST_NOTIFICATIONS` | Displays the silent persistent usage tracker notification. |
| `RECEIVE_BOOT_COMPLETED` | Restores protection and monitoring automatically after device reboots. |

---

## 🚀 Building & Installing

### Prerequisites
- Android Studio Ladybug (or newer) / IntelliJ IDEA
- JDK 17
- Android SDK Platform 36

### Build from Source

```bash
# Clone the repository
git clone https://github.com/afnan-nex/Stop-me.git
cd Stop-me

# Build the debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

### Install via ADB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 👤 Author

Developed with ❤️ by **AFNAN**
- GitHub: [@afnan-nex](https://github.com/afnan-nex)
- Repository: [https://github.com/afnan-nex/Stop-me](https://github.com/afnan-nex/Stop-me)

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
