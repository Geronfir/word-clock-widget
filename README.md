# Word Clock Widget

An Android home-screen widget that tells the time in **words** instead of digits —
"IT IS QUARTER PAST THREE", lit up in a letter-grid, exactly like a physical word
clock.

Built for reliability, battery efficiency and extensibility: the time-to-words
logic is a plain, fully unit-tested Kotlin library with no Android dependency, and
the widget is a thin rendering shell on top of it.

> Status: MVP complete. Word Grid and Flowing Text styles, per-widget
> configuration, offline-first, no background service.

---

## Features

- **Word Grid** — the classic lit/unlit word matrix (5×5 English grid).
- **Flowing Text** — the same time rendered as a readable phrase.
- **Per-widget configuration** — each widget instance keeps its own settings
  (12/24-hour, day period, style, colours).
- **Battery-friendly updates** — an inexact `AlarmManager` alarm ticks once a
  minute; there is **no** permanently running service.
- **Offline-first** — no network permission is requested and none is needed.
- **Lifecycle-safe** — survives reboot, timezone/locale changes, resizes,
  widget restore, and configuration deletion.
- **Accessible** — the lit phrase is exposed as a content description, and active
  words are bold as well as brighter, so state is never colour-only.

## Architecture

```
System Clock
    ↓
Time Source (java.time, device timezone)
    ↓
Time Representation Engine   ← pure Kotlin, no Android
    ↓
Localization / Language Rules
    ↓
Semantic Time Model (SemanticTime: structured, not a string)
    ↓
┌─────────────────────┐
│                     │
▼                     ▼
Word Grid Renderer   Flowing Text Renderer
│                     │
└──────────┬──────────┘
           ▼
      Android Widget (Jetpack Glance)
```

### Source layout

```
app/src/main/java/com/geronfir/wordclock/
├── engine/      Time logic + localization. No Android imports.
│   ├── WordKey.kt                  Language-neutral word keys
│   ├── SemanticTime.kt             Structured engine output
│   ├── TimeExpressionEngine.kt     Time → SemanticTime
│   ├── WordVocabulary.kt           English vocabulary
│   ├── WordGrid.kt                 5×5 English grid + active-cell mapping
│   ├── PhraseFormatter.kt          SemanticTime → readable phrase
│   ├── LocalizationRegistry.kt     Language lookup (extensible)
│   └── RepresentationStyle.kt      WORD_GRID | FLOWING_TEXT
├── settings/    Per-instance persistence (DataStore, namespaced by widget id)
├── widget/      Glance widget, receiver, per-minute scheduler, size resolver
└── config/      One-screen configuration activity
```

The engine has **no** Android imports, which is why all 53 tests run as plain JVM
tests in CI — no emulator, no instrumentation.

## Building

Building happens in **GitHub Actions** (see `.github/workflows/build.yml`); the
debug APK and test results are uploaded as workflow artifacts.

Locally, the same Gradle wrapper works if you have an Android SDK:

```bash
./gradlew testDebugUnitTest   # 53 unit tests
./gradlew assembleDebug       # app/build/outputs/apk/debug/*.apk
./gradlew lintDebug           # Android Lint
```

### Toolchain

| Component | Version |
|---|---|
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13 |
| Kotlin | 2.1.21 |
| Jetpack Glance | 1.1.1 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |
| JDK | 17 (CI) / 21 (local dev) |

## Testing

53 unit tests, all JVM-only:

| Suite | Covers |
|---|---|
| `TimeExpressionEngineTest` | o'clock / past / to, boundary minutes, hour carry, 12/24-hour, timezones, invalid input |
| `EnglishWordGridTest` | grid shape, unique tokens, active-cell mapping, shared FIVE/TEN cells |
| `EdgeCaseTest` | all 1440 minutes of the day, midnight/noon, DST transitions, leap day, rounding off |
| `LocalizationExtensibilityTest` | a second language with different word order and a different grid |
| `GridMetricsTest` | responsive size → typography mapping |

## Design decisions

See [`docs/DECISIONS.md`](docs/DECISIONS.md) for the reasoning behind the bigger
choices (Glance, AlarmManager, value-class workarounds, grid token sharing).

## License

Private project. All rights reserved.
