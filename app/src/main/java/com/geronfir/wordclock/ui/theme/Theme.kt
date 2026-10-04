package com.geronfir.wordclock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material 3 theme for the app UI.
 *
 * The XML theme ([R.style.Theme.WordClock]) stays a plain framework theme on
 * purpose: the Compose `material3` artifact ships no XML styles, so all
 * Material 3 theming happens here, in Compose.
 *
 * The baseline palette mirrors the widget's MIDNIGHT preset (light words on a
 * near-black background) so the app and its home-screen widget feel like one
 * product; on Android 12+ the system's dynamic colour takes over instead.
 */

private val MidnightActive = Color(0xFFE8E8EC)
private val MidnightBackground = Color(0xFF101014)

private val DaylightActive = Color(0xFF101014)
private val DaylightBackground = Color(0xFFF4F5F7)

/**
 * `onSurfaceVariant` is the colour Material 3 uses for *readable* secondary text
 * (supporting lines, unselected navigation labels, section captions), so it must
 * clear WCAG AA (4.5:1) against the surface. The widget's own inactive-word
 * colour does NOT: #3A3A42 on #101014 is only 1.68:1, which is fine for
 * unlit words in a grid (they are decoration, not prose) but unreadable for
 * prose. Hence separate constants — reusing the inactive-word colour here made
 * every supporting line nearly invisible on Android 8-11, where the dynamic
 * colour scheme is unavailable.
 */
private val MidnightOnSurfaceVariant = Color(0xFFA0A0AA) // 7.33:1 on #101014
private val DaylightOnSurfaceVariant = Color(0xFF50545C) // 6.97:1 on #F4F5F7

// `outline` is a border, not text: WCAG's 3:1 non-text threshold applies.
private val MidnightOutline = Color(0xFF70747E) // 4.06:1 on #101014
private val DaylightOutline = Color(0xFF8A8E96) // 3.01:1 on #F4F5F7

private val WordClockDarkColors = darkColorScheme(
    primary = MidnightActive,
    onPrimary = MidnightBackground,
    secondary = MidnightActive,
    onSecondary = MidnightBackground,
    tertiary = MidnightActive,
    onTertiary = MidnightBackground,
    background = MidnightBackground,
    onBackground = MidnightActive,
    surface = MidnightBackground,
    onSurface = MidnightActive,
    surfaceVariant = MidnightBackground,
    onSurfaceVariant = MidnightOnSurfaceVariant,
    outline = MidnightOutline,
)

private val WordClockLightColors = lightColorScheme(
    primary = DaylightActive,
    onPrimary = DaylightBackground,
    secondary = DaylightActive,
    onSecondary = DaylightBackground,
    tertiary = DaylightActive,
    onTertiary = DaylightBackground,
    background = DaylightBackground,
    onBackground = DaylightActive,
    surface = DaylightBackground,
    onSurface = DaylightActive,
    surfaceVariant = DaylightBackground,
    onSurfaceVariant = DaylightOnSurfaceVariant,
    outline = DaylightOutline,
)

/**
 * The app-wide Material 3 theme. Follows the system dark/light setting; on
 * Android 12+ it prefers the wallpaper-derived dynamic colour scheme.
 */
@Composable
fun WordClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> WordClockDarkColors
        else -> WordClockLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
