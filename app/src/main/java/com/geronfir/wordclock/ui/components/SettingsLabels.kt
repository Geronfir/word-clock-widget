package com.geronfir.wordclock.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.geronfir.wordclock.R
import com.geronfir.wordclock.engine.RepresentationStyle

/**
 * Human-readable labels shared by every screen that describes a widget setting.
 *
 * These live here rather than in one screen so the Home summary and the Display
 * controls can never disagree about what a setting is called, and so each label
 * is translated once (they resolve through [stringResource], never a hardcoded
 * string).
 */

@Composable
fun languageLabel(languageTag: String): String = stringResource(
    when (languageTag) {
        "id" -> R.string.config_language_id
        else -> R.string.config_language_en
    },
)

@Composable
fun styleLabel(style: RepresentationStyle): String = stringResource(
    when (style) {
        RepresentationStyle.WORD_GRID -> R.string.config_style_grid
        else -> R.string.config_style_flowing
    },
)

@Composable
fun fontLabel(fontScale: Float): String = stringResource(
    when {
        fontScale <= 0.8f -> R.string.config_font_small
        fontScale >= 1.4f -> R.string.config_font_large
        fontScale >= 1.2f -> R.string.config_font_medium
        else -> R.string.config_font_default
    },
)
