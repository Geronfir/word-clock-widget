# Rencana: Material 3 UI untuk Word Clock

> Tujuan: app-nya punya **menu sungguhan** dan tampilan **Material 3** (sekarang hanya teks polos + RadioButton jelek).
> Cara kerja: Hermes = leader + verifikator; subagent = pekerja; build diverifikasi lewat GitHub Actions (build Android tak bisa lokal di Termux).

## Ruang lingkup (disetujui user)

3 layar:
1. **Beranda** — preview jam LIVE (kata-kata berubah tiap menit), tombol "Tambah widget ke layar utama", ringkasan pengaturan.
2. **Tampilan** — gaya (Word Grid / Flowing Text), bahasa, format 12/24 jam, day period, ukuran huruf, warna (swatch).
3. **Tentang** — versi, penjelasan singkat, offline-first, tautan repo.

## Yang TIDAK disentuh

- `engine/**` — logika murni + sudah teruji. Jangan diubah.
- `widget/**` — renderer Glance sudah benar.
- `scripts/verify-apk.sh`, `docs/DECISIONS.md` (kecuali menambah ADR baru).

## Toolchain (SUDAH diverifikasi, jangan diubah tanpa alasan)

| Komponen | Versi | Catatan |
| --- | --- | --- |
| Kotlin | 2.1.21 | naikkan hanya bila perlu |
| AGP | 8.13.2 | |
| Compose BOM | 2026.09.00 | dari Google Maven (dl.google.com), BUKAN Maven Central |
| material3 | lewat BOM | 1.4.0 |
| activity-compose | 1.13.0 | |
| navigation-compose | 2.10.2 | |
| compileSdk/targetSdk | 35 | minSdk 26 |

**Aturan Compose Compiler:** pakai plugin `org.jetbrains.kotlin.plugin.compose` (versi = versi Kotlin). **JANGAN** tambah `composeOptions { kotlinCompilerExtensionVersion }` — itu gagal keras di Kotlin 2.x.

## Kontrak API yang boleh dipakai (jangan menebak)

```kotlin
// engine — SUDAH ADA, jangan diubah
data class TimeConfig(use24Hour: Boolean = false, roundToNearestFive: Boolean = true, includeDayPeriod: Boolean = false)
data class SemanticTime(hour24, minute, roundedMinute, phraseHour12, expressionType, minuteUnit, dayPeriod, use24Hour, activeWords: List<WordKey>)
enum class RepresentationStyle { WORD_GRID, FLOWING_TEXT }
enum class ThemePreset(@StringRes labelRes, activeColorArgb: Long, inactiveColorArgb: Long, backgroundColorArgb: Long)
    // MIDNIGHT, DAYLIGHT, AMBER, OCEAN; ThemePreset.apply(preset, settings); ThemePreset.matching(settings)

// settings — SUDAH ADA, jangan diubah
data class WidgetSettings(languageTag="en", use24Hour=false, includeDayPeriod=false,
    representationStyle=WORD_GRID, activeColorArgb, inactiveColorArgb, backgroundColorArgb, fontScale=1.0f)
    companion: DEFAULT, FONT_SCALES = [0.75f, 1.0f, 1.25f, 1.5f]
class WidgetSettingsStore(context) { suspend load(appWidgetId): WidgetSettings; suspend save(appWidgetId, settings); suspend delete(appWidgetId) }
```

`WidgetSettingsStore` adalah penyimpanan **per-widget**. Layar app (bukan config) tidak punya `appWidgetId` — untuk preview pakai `WidgetSettings.DEFAULT` atau settings instance pertama yang ditemukan.

## Pembagian tugas subagent

| # | Tugas | Output |
| --- | --- | --- |
| 1 | Fondasi: tambah Compose BOM + activity-compose + navigation-compose + material3 ke `gradle/libs.versions.toml` & `app/build.gradle.kts`; tema Material 3 (`Theme.WordClock` -> Material3); `strings.xml` baru (judul menu, label, deskripsi) | build hijau |
| 2 | Layar Beranda + navigasi (NavHost + bottom navigation 3 tujuan), preview jam live | build hijau |
| 3 | Layar Tampilan (segmented button gaya, switch, slider/segmented ukuran, swatch warna, pilihan bahasa) | build hijau |
| 4 | Layar Tentang + polish (tipografi, spacing, aksesibilitas, dark mode) | build hijau |

Aturan tiap subagent:
- Kerjakan HANYA file yang jadi lingkupnya; jangan sentuh `engine/**` dan `widget/**`.
- Jangan menjalankan `./gradlew` (tidak bisa di Termux). Verifikasi sintaks lewat compiler Kotlin lokal (lihat skill `android-app-ci-build` -> `references/local-kotlin-verify.md`).
- Jangan commit/push sendiri — laporkan diff-nya ke leader.
- Jangan mengarang API Compose/Material3. Kalau ragu, cek `context7` atau source Google Maven.

## Verifikasi (leader)

1. Commit + push ke branch `feat/material3-ui`.
2. `gh run watch` — build WAJIB hijau, artefak APK WAJIB ada.
3. `scripts/verify-apk.sh` + `aapt2 dump badging` — launcher ada, tidak ada permission INTERNET baru.
4. Uji unit tetap hijau (`testDebugUnitTest`) — engine tidak boleh rusak.
5. Baru laporkan ke user.

## Kriteria selesai

- [ ] Buka app -> ada menu (bukan cuma teks), 3 tujuan navigasi.
- [ ] Preview jam live di Beranda.
- [ ] Tampilan pakai komponen Material 3 (bukan RadioButton polos).
- [ ] CI hijau, APK terverifikasi, unit test engine tetap hijau.
- [ ] Tidak ada permission baru (tetap offline-first).
