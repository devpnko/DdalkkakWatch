# Changelog

All notable changes to DdalkkakWatch are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/).

## [0.1.0] — 2026-06-21

Initial open-source release.

### Added
- **BLE HID Boot Keyboard** — the watch pairs to a Mac as a driverless
  Bluetooth keyboard and sends `Opt+Cmd+X` to trigger Mac dictation.
- **Push-to-talk** — press & hold the watch screen to hold the shortcut,
  release to stop.
- **Double-tap = Enter** — accept dictation without touching the Mac.
- **Knock-Knock screen-off mode** — assign "딸깍 받아쓰기" to the Galaxy
  Watch Knock-Knock gesture; two wrist taps fire dictation without waking
  the screen (`QuickDictateActivity`).
- **Foreground service** (`DictationService`) keeps the BLE connection warm
  so Knock-Knock triggers instantly.
- **Onboarding wizard** (`onboarding/`) — a static site that recommends one
  of six audio-path combinations (Mac mic / earbuds / lavalier / watch mic /
  phone bridge / no-watch) and walks through setup + verification.
- Apache 2.0 license with WearMouse HID-pattern attribution.

### Known limitations
- Audio paths C4 (watch mic → roc-vad Wi-Fi) and C5 (phone bridge) are
  documented but not yet implemented.
- Install is via adb sideload (not yet on Galaxy Store).
- Double Pinch / Universal Gestures are Samsung-system-only and cannot be
  bound by third-party apps; Knock-Knock is the supported screen-off path.

[0.1.0]: https://github.com/devpnko/DdalkkakWatch/releases/tag/v0.1.0
