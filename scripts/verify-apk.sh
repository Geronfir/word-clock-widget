#!/usr/bin/env bash
# Asserts a built Glance-widget APK is actually usable:
#   1. it has a launcher activity (the app can be opened), and
#   2. its own Compose code was compiled by the Compose compiler (the widget can render).
# Both faults ship as a GREEN build with a broken APK, so this inspects the artifact.
# Usage: scripts/verify-apk.sh [path/to/app-debug.apk]
set -euo pipefail

APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
PKG_DIR="com/geronfir/wordclock"   # root package path under smali*/
fail() { echo "FAIL: $*" >&2; exit 1; }

[ -f "$APK" ] || fail "APK not found: $APK"

# --- Locate aapt2 (distro PATH, or the Android SDK build-tools) ---
if command -v aapt2 >/dev/null 2>&1; then
  AAPT2="aapt2"
elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/build-tools/35.0.0/aapt2" ]; then
  AAPT2="$ANDROID_HOME/build-tools/35.0.0/aapt2"
elif [ -x "${HOME}/android-sdk/build-tools/34.0.0/aapt2" ]; then
  AAPT2="${HOME}/android-sdk/build-tools/34.0.0/aapt2"
else
  fail "aapt2 not found (install aapt2 or set ANDROID_HOME)"
fi

# --- Check 1: launcher activity ---
if ! "$AAPT2" dump badging "$APK" | grep -q "launchable-activity:"; then
  fail "no launchable-activity — the app has no launcher icon and cannot be opened"
fi
echo "OK: launcher activity present"

# --- Check 2: Compose compiler ran on the app's own code ---
command -v apktool >/dev/null 2>&1 || fail "apktool not found (needed for the Compose check)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
apktool d -f -o "$WORK/dec" "$APK" >/dev/null 2>&1 || fail "apktool could not decode $APK"

if ! grep -rq "Landroidx/compose/runtime/Composer;" "$WORK"/dec/smali*/"$PKG_DIR"/ 2>/dev/null; then
  fail "app composables were NOT transformed by the Compose compiler (missing org.jetbrains.kotlin.plugin.compose) — widget will show 'Can't show content'"
fi
echo "OK: app composables carry a Composer parameter (Compose compiler ran)"

echo "PASS: $APK"
