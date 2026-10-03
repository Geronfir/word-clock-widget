# Architecture decisions

Short records of the choices that had real trade-offs, per the spec's rule 20
("if an implementation decision has meaningful trade-offs, document the decision
and its reasoning rather than silently choosing a potentially restrictive
approach").

---

## 1. Jetpack Glance over classic `AppWidgetProvider` + `RemoteViews`

**Decision:** build the widget with Jetpack Glance.

**Why:** Glance gives a declarative, composable API and — crucially — the same
`SemanticTime` model feeds both the grid and the flowing-text renderer without
duplicating layout logic. Hand-rolled `RemoteViews` would mean two parallel view
trees and manual state plumbing.

**Trade-off:** Glance is a thinner abstraction over `RemoteViews` with some API
gaps (see decision 3 and 4). It also raises the effective floor to API 26, which
is acceptable: API 26 (Android 8) is the practical minimum for Glance and covers
the overwhelming majority of devices.

---

## 2. `AlarmManager` instead of `updatePeriodMillis` or a service

**Decision:** schedule an inexact `AlarmManager` alarm every 60 s; the receiver
re-renders and re-arms. `updatePeriodMillis` is left at 30 minutes purely as a
backstop.

**Why:** Android caps `updatePeriodMillis` at a 30-minute minimum, which is far
too coarse for a clock. The spec forbids a permanently running service. An inexact
repeating alarm is the platform-supported middle ground.

**Trade-off:** inexact alarms may drift by seconds, so the displayed minute can
flip slightly after the true minute boundary. For a word clock this is invisible
and it is far kinder to the battery than exact alarms (which on Android 12+ also
need special permission). Alarms are re-armed on `BOOT_COMPLETED`,
`TIME_SET`, `TIMEZONE_CHANGED`, `LOCALE_CHANGED`, `DATE_CHANGED` and
`MY_PACKAGE_REPLACED`, since any of those can drop a repeating alarm.

---

## 3. Fixed-width grid cells instead of `GlanceModifier.defaultWeight()`

**Decision:** each grid cell is a fixed-width box (`GlanceModifier.width(dp)`),
not a weighted cell.

**Why:** `defaultWeight()` does not exist in the pinned Glance 1.1.1. Fixed-width
cells keep columns aligned at every widget size and behave identically on every
API level.

**Trade-off:** very wide widgets leave some horizontal slack rather than stretching
the cells. Acceptable for an MVP; if Glance gains `defaultWeight()` on the pinned
line, cells can be swapped to weighted without touching the engine.

---

## 4. Measuring widget size from `AppWidgetManager`, not `LocalSize`

**Decision:** read the widget's size in `provideGlance` via
`AppWidgetManager.getAppWidgetOptions()` and pass plain dp floats into the
renderer.

**Why:** reading Glance's `LocalSize` inside a composable crashes the Kotlin
2.1.21 / JVM IR backend with *"Couldn't inline method call:
CompositionLocal.get-current"*. `LocalSize` is a `DpSize`, which is a value class,
and the inliner cannot handle it when it is passed as a parameter. Measuring
outside the composition sidesteps the bug entirely.

**Trade-off:** the first render uses a fallback size until the host reports
options. `onAppWidgetOptionsChanged` triggers an immediate re-render once real
dimensions are known, so the user never sees a stuck layout. The option values
are already in dp ("in dips" per the platform docs), so they are passed through
unchanged — dividing by `displayMetrics.density` would shrink every widget and
pin it to the smallest font bucket.

---

## 5. Grid token sharing (FIVE / TEN used twice)

**Decision:** the English grid stores `FIVE` and `TEN` exactly once each; they
serve as both minute units and hour words.

**Why:** this mirrors how a physical English word clock shares letters between
rows, keeps the grid at 5×5, and guarantees a single lit cell per word — no
duplicated highlight when the phrase is "twenty five past five".

**Trade-off:** day-period phrases ("in the morning") do not fit a single cell, so
they are rendered by the flowing-text style and via the optional AM/PM grid cells
instead of being crammed into the matrix.

---

## 6. Configuration scoped per widget instance

**Decision:** DataStore keys are namespaced as `widget_<appWidgetId>_<name>`, and
`onDeleted` removes the instance's keys while `onRestored` migrates them.

**Why:** the spec forbids a single global settings blob. Namespacing lets two
widgets on the same home screen show different languages, formats and colours,
and cleaning up on delete prevents a reused `appWidgetId` from inheriting stale
settings.

**Trade-off:** slightly more storage churn than one shared blob, which is
irrelevant at this scale.

---

## 7. Engine has zero Android dependencies

**Decision:** everything in `engine/` is pure Kotlin + `java.time`.

**Why:** it makes the time-to-words logic testable in milliseconds on a plain JVM,
so CI needs no emulator. All 53 tests — including a full sweep of all 1,440 minutes
of the day — run in seconds.

**Trade-off:** the engine cannot use Android's `Time`/`Calendar` helpers. This is
a benefit, not a cost: `java.time` is the correct API and behaves identically on
every supported API level via desugaring (minSdk 26 already has it natively).

---

## 8. Compose compiler plugin is mandatory (Kotlin 2.x)

**Decision:** apply `org.jetbrains.kotlin.plugin.compose` (version pinned to the Kotlin version) and
set `buildFeatures.compose = true`.

**Why:** Glance is built on the Compose runtime. Since Kotlin 2.0 the Compose compiler is a separate
Gradle plugin; without it `@Composable` functions compile with no `Composer` parameter and Glance's
composition throws at render time, so the widget shows the host error card "Can't show content".
The failure is invisible at build time (CI stays green), which is why `scripts/verify-apk.sh` asserts
the transformed signature on the built APK.

