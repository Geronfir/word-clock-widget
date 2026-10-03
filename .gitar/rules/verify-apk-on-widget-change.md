---
title: "Verify APK on widget or build changes"
description: "Remind the author to keep the launcher + Compose-compiler APK assertions green when widget, manifest or build files change"
when: "A PR modifies app/src/main/AndroidManifest.xml, anything under app/src/main/java/com/geronfir/wordclock/widget/, app/build.gradle.kts, gradle/libs.versions.toml, or scripts/verify-apk.sh"
actions: "Post a comment noting that scripts/verify-apk.sh must pass and that CI's 'Verify APK (launcher + compose compiler)' step is the merge gate; do not merge while it is red"
---

# Verify APK on widget or build changes

This repository ships a home-screen widget whose two historical failures (the
app will not open; the widget shows "Can't show content") both build **green**
and only fail at runtime. `scripts/verify-apk.sh` catches them by inspecting the
built APK: it asserts a launchable activity exists and that the app's own
composables were transformed by the Compose compiler.

When a PR touches widget code, the manifest, or the Gradle build files:

1. Confirm the PR does not remove `alias(libs.plugins.compose.compiler)` or
   `buildFeatures { compose = true }` from `app/build.gradle.kts`.
2. Confirm the PR does not remove the launcher `<intent-filter>`
   (`MAIN` / `LAUNCHER`) from `AndroidManifest.xml`.
3. Post a comment reminding the author that CI's
   **"Verify APK (launcher + compose compiler)"** step is the merge gate.
