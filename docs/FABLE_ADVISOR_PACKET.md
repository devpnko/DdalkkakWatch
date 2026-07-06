# Fable Advisor Packet

## Mission

Guide DdalkkakWatch from local near-complete app to a conservative open-source
release. The long-term product can become a hardware clicker, but this repo
should only claim the current Wear OS Bluetooth HID trigger.

## Local Availability Note

On 2026-07-07 KST, the local environment did not expose standalone `fable`,
`fable5`, or `fable-pm` commands. Fable 5 is available through Claude Code:

```bash
claude --model fable --effort max
```

The active visible advisor pane for this release pass is:

```text
tmux session: ddalkkak-fable-pm
cwd: repository root
mode: Fable 5 max, plan/advisor
```

## Decisions Needed

1. Public positioning:
   - Allowed: Wear OS watch sends a Mac dictation hotkey as a Bluetooth HID
     keyboard.
   - Not allowed without proof: hardware clicker, watch microphone path, phone
     relay path, broad non-Mac compatibility.

2. Terminology:
   - Prefer "Bluetooth HID keyboard profile via Android BluetoothHidDevice".
   - Avoid loose BLE-HID or Classic Bluetooth claims unless the release
     evidence and Android API behavior are cited precisely.

3. Release quality bar:
   - CI must run lint and debug assemble.
   - Onboarding links must be checked.
   - A real watch-to-Mac proof must be captured before a non-beta release.

4. Scope separation:
   - DdalkkakWatch is the open-source watch trigger.
   - Hardware clicker exploration should stay in a separate roadmap or repo
     until there is firmware, enclosure, and pairing proof.

## Current Fixes Applied

- Android lint blocker fixed by adding an explicit AndroidX Fragment dependency.
- Foreground dictation service start moved until after Bluetooth permissions are
  granted and HID registration begins.
- Bluetooth calls now guard Android 12+ runtime permissions before register,
  connect, and key report operations.
- Public docs no longer claim BLE-HID or "working" for setup paths without a
  release proof artifact.
- CI now runs `:app:lintDebug` before building the debug APK.
- Security policy and release checklist were added.
- `ddalkkak-fable-pm` was launched as the visible Fable advisor session.
- Fable advisor verdict was captured in `docs/FABLE_ADVISOR_VERDICT.md`.

## Builder Orders

1. Keep runtime changes narrowly scoped.
2. Do not add a Mac companion app to this repo.
3. Do not claim C4/C5 as shipped.
4. Do not commit APKs, keystores, logs, screenshots, or private device IDs.
5. Require direct device proof before changing "supported" to "verified".

## Expected Advisor Output

Return one of:

- `APPROVE_PUBLIC_BETA`: safe to publish with conservative wording.
- `APPROVE_RELEASE`: safe to tag a release because device proof is attached.
- `BLOCK`: list exact blockers and the file or command that proves each one.

Use `docs/OPEN_SOURCE_RELEASE.md` as the acceptance checklist.
