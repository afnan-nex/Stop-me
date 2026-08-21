# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.1] - 2026-08-21

### Added
- **Two-Phase Reset & Removal Challenge**: Gated the home screen app countdown reset (long-press) and app removal in settings behind the full Two-Phase Challenge:
  - **Phase 1**: 150 circular taps with haptic feedback and real-time counter.
  - **Phase 2**: Mindful pledge writing popup dialog with live word-by-word comparison and error detection.
  - Reset and removal actions are strictly blocked until both phases are completed 100%.

### Fixed
- **Countdown Banner Appearance & Centering**: Fixed Pill and Bold countdown banners to be centered horizontally at the top of the screen with a proper compact pill (stadium) shape instead of filling full screen width.
- **Top App Bar / Notch Spacing**: Resolved double status bar insets across `Apps`, `Charts`, and `Settings` screens, eliminating the excessive top gap and aligning headings directly below the device notch/cutout.

## [1.1.0] - 2026-08-21

### Added
- **Blocking Overlay Dismissal & Close App**: Added a prominent "Close app" button and top-right close (X) icon to the blocking screen for immediate return to the Home launcher.
- **Hardware & Gesture Back Button Handling**: Intercepted `KeyEvent.KEYCODE_BACK` on full-screen overlay views to safely dismiss blocking dialogs without trapping users.
- **WindowManager Lifecycle Safeguards**: Wrapped all window addition and removal operations in error-handled boundaries to prevent system service crashes.

### Fixed
- **Writing Challenge on Countdown Reset**: Long-pressing an app now strictly presents the `WritingChallengeDialog` pledge popup, ensuring timer resets cannot be performed without completing the full writing challenge.
- **Overlay Lingering Bug**: Ensured that leaving a protected app immediately dismisses all blocking and warning overlays from non-protected apps and the Home screen.
- **Accurate Timing Labels**: Improved time string formatting so reset applications accurately display 0m usage instead of rounded fractions.

## [1.0.1] - 2026-08-21

### Changed
- **Transient Usage Notification**: Removed continuous idle "Stop me is active" notification. The notification is now displayed strictly while a protected application is actively in use and is immediately dismissed when switching to other apps or returning to the home screen.
- **Application Display Name**: Updated branding and string references from "Stop-me" to "Stop me".
- **Settings Architecture**: Relocated the protected apps management list inside a dedicated "Apps" action popup under the Data section.

### Fixed
- **Countdown Reset Synchronization**: Fixed issue where in-memory elapsed timing sessions in `ForegroundAppTracker` were not pausing upon opening the app, ensuring that tapping "Reset" in the countdown dialog reliably resets today's usage back to 0.

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
