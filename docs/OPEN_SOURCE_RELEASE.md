# Open Source Release Checklist

This checklist keeps the first public release honest. The public scope is a
Wear OS watch app that acts as a Bluetooth HID keyboard trigger for Mac
dictation. The watch-microphone audio path, phone relay path, and hardware
clicker are future work unless a release explicitly proves otherwise.

## Required Gates

1. Build and lint pass locally:

   ```bash
   ./gradlew :app:lintDebug
   ./gradlew :app:assembleDebug
   ```

2. Onboarding checks pass:

   ```bash
   node --check onboarding/app.js
   cd onboarding
   for f in index.html paths/*.html; do
     d=$(dirname "$f")
     for l in $(grep -oE 'href="[^"#]+"' "$f" | grep -vE 'http|mailto' | sed 's/href="//;s/"//'); do
       case "$l" in /*) continue;; esac
       test -f "$d/$l" || exit 1
     done
   done
   ```

3. Public claims match evidence:

   ```bash
   rg -n "[w]orks|[v]erified|prod[u]ction|BLE[ ]HID|Bluetooth[ ]Classic|driverless[ ]BLE|검[증]" README.md CHANGELOG.md CONTRIBUTING.md NOTICE onboarding .github app/src
   ```

   Keep C1, C2, and C6 described as supported setup paths unless fresh device
   proof is attached to the release. Keep C4 and C5 marked as coming soon.

4. Device proof is captured before tagging:

   Track this in `docs/DEVICE_PROOF.md`.

   - Watch model and Wear OS version.
   - Mac model and macOS version.
   - `adb install -r app/build/outputs/apk/debug/app-debug.apk` result.
   - First-run Bluetooth permission grant.
   - Pairing proof for the `DdalkkakWatch` Bluetooth device.
   - Press-and-hold sends `Opt+Cmd+X`, then release stops hold.
   - Double-tap sends Enter.
   - Knock-Knock entry either works or is clearly marked experimental.
   - Logcat excerpt with private identifiers removed.

5. Repository hygiene is clean:

   ```bash
   git status --short
   rg -n --hidden -g '!docs/OPEN_SOURCE_RELEASE.md' -g '!**/build/**' -g '!.git/**' "TODO|FIXME|SECRET|TOKEN|PASSWORD|PRIVATE KEY|/U[s]ers/" .
   ```

   Do not commit APKs, keystores, local SDK paths, device logs, or private
   Bluetooth addresses.

6. Advisor gate:

   - Ask Fable 5 to review `docs/FABLE_ADVISOR_PACKET.md` when available.
   - If Fable is unavailable, publish only after the gates above pass and keep
     the first release conservative: no hardware claims, no watch-mic claims,
     no unproven compatibility promises.

## Release Steps

1. Configure the public GitHub remote.
2. Ensure GitHub Actions passes on `main`.
3. Enable GitHub Pages for `onboarding/` if desired.
4. Create a release tag after device proof is attached to the release notes.
5. Upload the debug APK only if the release notes clearly say it is unsigned
   debug/dev tooling and not a Play Store build.

## No-Go Conditions

- Lint or assemble fails.
- Onboarding links are broken.
- A public doc claims C4, C5, hardware clicker, or watch-mic support without
  matching shipped code and device proof.
- Logs or screenshots expose private device identifiers.
- Fable or another advisor raises a public-claim blocker that has not been
  resolved or explicitly downgraded in the docs.
