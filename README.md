# DdalkkakWatch ⌚→💻

> Press your watch, dictate into your Mac. A Wear OS app that turns your
> Galaxy Watch (or any Wear OS watch) into a wireless push-to-talk trigger
> for Mac voice dictation.

**딸깍** (*ddalkkak*, the Korean onomatopoeia for a single click) sends a
Bluetooth HID keyboard shortcut (`Opt+Cmd+X`) to your Mac. That shortcut
fires your dictation app (OpenTypeless / Superwhisper / macOS Dictation),
your Mac's mic captures your voice, and the text lands in whatever field
has focus. **No app on the Mac side — the watch looks like a Bluetooth
keyboard.**

[![build](https://github.com/devpnko/DdalkkakWatch/actions/workflows/build.yml/badge.svg)](https://github.com/devpnko/DdalkkakWatch/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Wear%20OS%205-green.svg)](https://wearos.google.com/)

---

## Why

Voice dictation on a Mac is great, but reaching for the keyboard shortcut
breaks your flow — especially when your hands leave the keyboard (whiteboard,
standing, pacing, coffee). A watch press is always within reach.

The hard part isn't the mic — it's the **trigger**. DdalkkakWatch solves the
trigger with Android's `BluetoothHidDevice` keyboard profile, and lets you
choose how the audio gets in (built-in mic, earbuds, lavalier, or — coming —
the watch's own mic path).

## Quick start

1. **Build & install** to your watch (adb sideload — see [onboarding](onboarding/paths/c1.html)):
   ```bash
   ./gradlew :app:assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Pair** the watch to your Mac over Bluetooth (the watch advertises as
   `DdalkkakWatch`).
3. **Set your Mac dictation hotkey** to `Opt+Cmd+X`.
4. **Press and hold** the watch screen → speak → release. Text appears.
   **Double-tap** = Enter.

> 🇰🇷 한국어 셋업은 **온보딩 마법사**가 안내합니다 → `onboarding/index.html`
> (워치 연결부터 받아쓰기까지 4단계 + 6가지 조합 추천)

## Usage modes

| Gesture | Action |
|---|---|
| **Press & hold** the screen | Hold `Opt+Cmd+X` (push-to-talk) → release to stop |
| **Double-tap** | Enter (accept dictation) |
| **Knock-Knock** (two wrist taps) | Screen-off dictation toggle — see [guide](onboarding/paths/knock-knock.html) |

## Audio paths (pick what fits)

The watch is the trigger; the audio can come from anywhere. The onboarding
wizard recommends one based on your gear and use case.

| ID | Setup | Quality | Status |
|---|---|---|---|
| **C1** | Mac built-in mic + watch PTT | ⭐⭐⭐ | ✅ supported |
| **C2** | Galaxy Buds / AirPods + watch PTT | ⭐⭐⭐⭐ | ✅ supported |
| **C3** | USB-C lavalier (Lark M2 / DJI) + watch PTT | ⭐⭐⭐⭐⭐ | 💡 recommended |
| **C4** | Watch mic → roc-vad Wi-Fi UDP → Mac virtual mic | ⭐⭐⭐ | 🚧 coming |
| **C5** | Watch → phone → Mac | ⭐⭐⭐ | 🚧 coming |
| **C6** | Mac hotkey only (no watch) | ⭐⭐⭐ | ✅ supported |

## Architecture

```
Watch (Wear OS)                         Mac (no app needed)
┌──────────────────────────┐
│ MainActivity (PTT/dbl-tap)│           ┌────────────────────┐
│ QuickDictateActivity      │ Bluetooth │ Bluetooth keyboard │
│   (Knock-Knock, no UI)    │──Boot────▶│   = Opt+Cmd+X      │
│ DictationService (keep-   │  Keyboard │        ↓           │
│   alive, Foreground)      │           │ OpenTypeless /     │
│ BleHidManager             │           │ Superwhisper /     │
│ HidReportDescriptor       │           │ macOS Dictation    │
└──────────────────────────┘           └────────────────────┘
```

- **Bluetooth HID keyboard profile** via Android `BluetoothHidDevice`,
  driverless on macOS.
- **Foreground Service** keeps the connection warm so Knock-Knock avoids most
  cold-start delay.
- 8-byte HID report: `[modifier, reserved, keycode×6]`.

## Requirements

- Wear OS 5+ watch (developed on Galaxy Watch Ultra). minSdk 33.
- A Mac with a dictation app bound to `Opt+Cmd+X`.
- JDK 17 + Android SDK (platform 34, build-tools 34) to build.

## Tested Environment

The first public beta is based on owner-reported active use with a Galaxy Watch
Ultra and a MacBook Pro. See [device proof](docs/DEVICE_PROOF.md) for the
current proof status and the artifacts required before tagging a release.

## Onboarding wizard

`onboarding/` is a static site (HTML/JS, no build) you can open locally or
host on GitHub Pages. It walks anyone through device → scenario → recommended
audio path → setup → verification.

```bash
cd onboarding && python3 -m http.server 7788   # → http://127.0.0.1:7788/
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Small PRs welcome.
Release history in [CHANGELOG.md](CHANGELOG.md).

## License

[Apache License 2.0](LICENSE). Bluetooth HID patterns informed by Google's
[WearMouse](https://github.com/google/wearmouse) — see [NOTICE](NOTICE).
