# Device Proof

Status: owner-reported active use, public beta candidate.
Date: 2026-07-07 KST

This file records the real-device environment for the first public beta. It is
not a broad compatibility claim.

## Tested Environment

| Item | Value | Source |
|---|---|---|
| Watch | Galaxy Watch Ultra | Owner report |
| Wear OS / One UI Watch | To be filled before tag | Pending |
| Host | MacBook Pro, Mac16,5 | Local system profile |
| Host OS | macOS 26.5.2, build 25F84 | Local system profile |
| Dictation app | To be filled before tag | Pending |
| App install path | adb sideload / debug APK | Current beta setup |

Private host identifiers such as serial number, hardware UUID, provisioning
UDID, Bluetooth addresses, and host names are intentionally not recorded here.

## Owner-Reported Working Paths

- Galaxy Watch Ultra pairs to the Mac as the DdalkkakWatch Bluetooth HID
  keyboard trigger.
- Press-and-hold on the watch triggers Mac dictation through `Opt+Cmd+X`.
- Release stops the held shortcut.
- Double-tap sends Enter.
- Knock-Knock screen-off entry is used in the owner workflow.

## Artifacts To Attach Before Tagging

- Redacted `adb logcat` excerpt showing HID registration, connect, press,
  release, and QuickDictate paths.
- Short screen recording or screenshots showing pairing and Mac text entry.
- Wear OS / One UI Watch version.
- Dictation app and hotkey settings.
- Manual permission-denied check: launching QuickDictate without Bluetooth
  permissions routes to MainActivity and does not start the foreground service.

## Scope Boundary

This proof supports a conservative public beta for the Wear OS watch trigger.
It does not prove external-user reproduction, non-Mac host support, watch-mic
audio, phone relay, Play Store readiness, or hardware clicker support.
