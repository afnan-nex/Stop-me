# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-21

### Added
- **Core 30-Minute Daily Limiter**: Monotonically timed daily usage limit of 30 minutes per protected package, resetting at local midnight.
- **Accessibility Foreground Monitoring**: Service tracking active applications via `rootInActiveWindow` and window state events with intelligent transient overlay and IME filtering.
- **Independent Per-App Timers**: Isolated usage tracking and storage for each protected package to prevent cross-app timing leakage.
- **Screen & Lock-Aware State Engine**: `BroadcastReceiver` integration pausing active timers on screen-off or lock state and resuming upon user unlock.
- **Silent Persistent Usage Notification**: Foreground service notification dynamically reporting active package usage in `Minutes used (MM:SS)` format without sound, vibration, or popups.
- **Full-Screen Blocking Overlay**: Instant overlay triggered upon budget exhaustion that intercepts interactions and navigates the user to the Home screen.
- **T-30s Warning Countdown**: Configurable top banner (Pill, Minimal, Bold) alerting users 30 seconds before app limit exhaustion.
- **Two-Phase Unlock Challenge Flow**:
  - Phase 1: 150-tap haptic challenge.
  - Phase 2: Material 3 popup dialog requiring exact pledge text reproduction.
- **Live Word-by-Word Pledge Verification**: Real-time comparison engine supporting in-progress word typing, prefix checking, and detailed mismatch error feedback.
- **App-Specific Extra Time Grants**: Challenge completion awards +30 minutes exclusively to the targeted package without unlocking other protected apps.
- **Local App Icon Rendering**: Custom Compose `AppIconView` extracting local installed application drawables with rounded corners and fallback Material vector icons.
- **App Selector Sheet**: Bottom sheet listing all launchable installed apps with search filtering and manual package entry support.
- **Protected App Management in Settings**: Dedicated "Apps" management popup under the Data section allowing removal of protected packages via the unlock pledge challenge.
- **Long-Press Countdown Reset**: Context dialog on protected app items enabling immediate reset of today's elapsed timer back to the full 30-minute allowance.
- **Usage Statistics & Canvas Charts**: Visual analytics tab rendering daily bar charts and 7-day usage comparisons with clean canvas drawing.
- **Theme & Customization**: Support for Light, Dark, and System theme modes, customizable warning sounds, vibration toggles, and unlock challenge switches.
- **Data Backup & Restore**: Secure local JSON export and import supporting manual backups with schema validation.
