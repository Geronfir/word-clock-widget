package com.geronfir.wordclock.settings

import androidx.annotation.StringRes
import com.geronfir.wordclock.R

/**
 * A named colour scheme for the widget: active-word, inactive-word and background
 * colours. The configuration screen shows these as simple choices instead of
 * colour pickers, which keeps the screen small and the result predictable.
 *
 * Colours are stored per instance as raw ARGB longs (see [WidgetSettings]), so a
 * widget keeps its scheme even if this list changes later.
 */
enum class ThemePreset(
    @StringRes val labelRes: Int,
    val activeColorArgb: Long,
    val inactiveColorArgb: Long,
    val backgroundColorArgb: Long,
) {
    MIDNIGHT(R.string.theme_midnight, 0xFFE8E8EC, 0xFF3A3A42, 0xFF101014),
    DAYLIGHT(R.string.theme_daylight, 0xFF101014, 0xFFB8BCC4, 0xFFF4F5F7),
    AMBER(R.string.theme_amber, 0xFFFFB300, 0xFF4A3A12, 0xFF1A1206),
    OCEAN(R.string.theme_ocean, 0xFF7FD4FF, 0xFF2A4250, 0xFF07131A),
    ;

    companion object {
        /** Picks the preset whose colours match [settings], or MIDNIGHT as a fallback. */
        fun matching(settings: WidgetSettings): ThemePreset =
            entries.firstOrNull {
                it.activeColorArgb == settings.activeColorArgb &&
                    it.inactiveColorArgb == settings.inactiveColorArgb &&
                    it.backgroundColorArgb == settings.backgroundColorArgb
            } ?: MIDNIGHT

        /** Applies this preset's colours to [settings], keeping everything else. */
        fun apply(preset: ThemePreset, settings: WidgetSettings): WidgetSettings = settings.copy(
            activeColorArgb = preset.activeColorArgb,
            inactiveColorArgb = preset.inactiveColorArgb,
            backgroundColorArgb = preset.backgroundColorArgb,
        )
    }
}
