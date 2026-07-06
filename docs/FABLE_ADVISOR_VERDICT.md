# Fable Advisor Verdict

Date: 2026-07-07 KST
Mode: FCDFSM
Advisor surface: `ddalkkak-fable-pm`

## Verdict

`APPROVE_PUBLIC_BETA`, conditional.

Code, docs, and local gates are acceptable for an open-source public beta
candidate. This is not a final release/tag approval yet.

## Architecture Decision

Keep the current architecture:

- Wear OS app registers through Android `BluetoothHidDevice`.
- `DictationService` keeps the Bluetooth HID connection warm after permissions.
- `QuickDictateActivity` is the screen-off Knock-Knock entry point.
- The future hardware clicker remains a separate track and should not add
  abstractions or public claims to this repo yet.

## Allowed Claims

- DdalkkakWatch uses Android `BluetoothHidDevice` to act as a Bluetooth HID
  keyboard trigger.
- The app sends the Mac dictation shortcut and supports press-and-hold PTT,
  double-tap Enter, and a screen-off Knock-Knock entry point.
- Local lint, debug build, unit tests, onboarding syntax, and onboarding link
  checks pass.
- The owner can state personal real-device usage only with the exact tested
  watch, Wear OS, Mac, and macOS versions attached.

## Not Yet Allowed

- Production-ready.
- Works on every Mac or every host OS.
- Instant or zero-latency Knock-Knock.
- Verified by external users.
- Battery/runtime stability claims.
- Watch microphone, phone relay, or hardware clicker support as shipped.

## Remaining Release Blockers

1. No git remote is configured.
2. No release tag exists.
3. No real-device proof artifact is attached yet.
4. GitHub Actions has not run on the future public remote.

## Builder Orders Reflected

- Runtime permission and FGS guards were tightened.
- PTT lifecycle cleanup was added.
- `BLUETOOTH_SCAN` was removed from the app permission gate.
- Bluetooth host detection and log redaction were improved.
- Claim wording was downgraded to public-beta-safe language.
- Changelog was moved back to `Unreleased`.
- CI debug APK upload was limited to push builds with short retention.
- Host classification and Bluetooth address redaction now have unit tests.

## Proof Gates Before Tagging

- Real watch-to-Mac proof: pairing, press-and-hold, release, double-tap, and
  Knock-Knock behavior.
- Redacted `adb logcat` proof for HID registration and send/release paths.
- Manual permission-denied proof: QuickDictate routes to MainActivity and does
  not start the foreground service without Bluetooth permissions.
- Public GitHub Actions green on the configured remote.
- Final leak and public-claim scans clean.

## Stop Conditions

Stop release and re-open advisor review if:

- HID registration fails on the real device.
- Any proof artifact exposes device identifiers, Bluetooth addresses, or host
  names.
- CI differs from local lint/build results.
- A new runtime permission is required.
- A credible HID input-injection abuse scenario appears in review.
