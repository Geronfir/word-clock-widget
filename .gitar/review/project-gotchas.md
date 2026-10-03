# Word Clock Widget — review gotchas

A small Android app: a **Jetpack Glance** home-screen widget plus a **UI-free
Kotlin engine** that turns a time into words. Keep these invariants in mind.

## 1. `engine/` must stay Android-free
Everything under `app/src/main/java/com/geronfir/wordclock/engine/` is pure
Kotlin + `java.time`. It must not import `android.*` or `androidx.*`. This is what
lets all tests run on a plain JVM with no emulator. Flag a new Android import in
`engine/` as a blocker.

## 2. The Compose compiler plugin is mandatory
`app/build.gradle.kts` must keep `alias(libs.plugins.compose.compiler)` and
`buildFeatures { compose = true }`. Since Kotlin 2.0 the Compose compiler is a
separate Gradle plugin; if it is removed, `@Composable` code compiles with no
`Composer` parameter and the widget renders "Can't show content" while CI stays
green. Removing either line is a blocker.

## 3. Widget size comes from `SizeMode.Exact` + `LocalSize`
`WordClockWidget` declares `sizeMode = SizeMode.Exact` and reads `LocalSize.current`
inside the composition, converting it to `Float` on the same line. `LocalSize` is a
`DpSize` (value class); passing it as a composable **parameter** crashes the Kotlin
2.1.21 JVM IR backend ("Couldn't inline method call: CompositionLocal.get-current").
So: read it, convert to `Float`/dp immediately, and never hand the `DpSize` across a
composable boundary. Do not reintroduce `AppWidgetManager.getAppWidgetOptions()` for
sizing — the platform docs say that approach "doesn't work in all situations", it
lagged behind resizes, and some launchers report those ints in pixels, not dp.

## 4. Resize is handled by `SizeMode.Exact`, not a manual update
Do not call `updateAll` from `onAppWidgetOptionsChanged` to react to a resize:
`SizeMode.Exact` already re-composes with the new size. A manual update there only
duplicates work and adds latency while the user drags the resize handle.

## 5. Offline-first, no background service
`AndroidManifest.xml` must not request `INTERNET` (or any network permission).
The widget must not start a long-running service; per-minute refresh uses an
inexact `AlarmManager` alarm (`widget/WidgetUpdateScheduler.kt`).

## 6. Per-widget settings are namespaced
Settings are stored per `appWidgetId` in `settings/WidgetSettingsStore.kt`. Never
introduce a single global settings blob; two widgets must be able to differ.

## 7. Tests are JVM-only and TDD
New logic goes in a pure function first and gets a JUnit 4 test under
`app/src/test/java/...` before or with the implementation. Do not add
instrumentation tests for logic that can be pure.

## 8. The APK is verified by `scripts/verify-apk.sh`
Any change touching the manifest, `app/build.gradle.kts`,
`gradle/libs.versions.toml`, or widget code must keep `scripts/verify-apk.sh`
passing (it asserts a launcher activity exists and that the app's own composables
carry a `Composer` parameter).
