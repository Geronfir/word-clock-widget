package com.geronfir.wordclock.widget.render

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Maps the current widget size to typography and padding.
 *
 * The size is computed **continuously** from the widget's real size rather than
 * from fixed buckets. Buckets looked fine at the sizes we happened to test, but
 * they broke at the extremes: a small widget kept a bucket whose 5-column row was
 * wider than the widget (the text overflowed its parent), and a large one could
 * snap to a size that no longer matched the box.
 *
 * Everything here is a pure function of the widget size, so it can be unit-tested
 * on a plain JVM and never has to guess.
 */
data class GridMetrics(
    val fontSize: TextUnit,
    val cellWidth: Dp,
    val padding: Dp,
)

/** Absolute ceiling so a huge widget cannot render one giant, unreadable word. */
internal const val MAX_FONT_SP = 48f

/**
 * Default size as a fraction of the largest size that still fits.
 *
 * 0.6 leaves enough headroom that even the largest offered font-size preference
 * (1.5x) still fits under the safe maximum.
 */
private const val FIT_HEADROOM = 0.6f

/**
 * Metrics for the word-grid style.
 *
 * The grid is [columns] x [rows] cells; every row must fit inside the widget, so
 * the cell width is derived from the width (minus padding). The largest safe font
 * is the smaller of "fits a cell's width" and "fits a row's height", and the
 * returned font is a fraction of that, scaled by the user's preference — but it is
 * **never allowed to exceed the safe maximum**, so the matrix cannot spill outside
 * the widget at any size the user drags it to.
 *
 * @param fontScale the user's font-size preference (1.0 = the default size).
 */
fun gridMetricsFor(
    widthDp: Float,
    heightDp: Float,
    fontScale: Float = 1.0f,
    columns: Int = 5,
    rows: Int = 5,
): GridMetrics {
    val width = saneSize(widthDp)
    val height = saneSize(heightDp)
    val cols = columns.coerceAtLeast(1)
    val rowCount = rows.coerceAtLeast(1)

    val padding = (minOf(width, height) * 0.03f).coerceIn(2f, 12f)
    val usableWidth = (width - 2f * padding).coerceAtLeast(1f)
    val usableHeight = (height - 2f * padding).coerceAtLeast(1f)

    val cellWidth = usableWidth / cols
    // A cell holds one word (up to ~7 characters like "QUARTER"); ~0.24 of the
    // cell width is the widest a font can be and still keep that word inside its
    // cell.
    val maxFontFromWidth = cellWidth * 0.24f
    val maxFontFromHeight = (usableHeight / rowCount) * 0.6f
    val maxSafeFont = minOf(maxFontFromWidth, maxFontFromHeight)

    return GridMetrics(
        fontSize = safeFont(maxSafeFont, fontScale),
        cellWidth = cellWidth.dp,
        padding = padding.dp,
    )
}

/**
 * Metrics for the flowing-text style.
 *
 * There is no grid here, just one phrase. The largest safe font is the size at
 * which [charCount] characters fit across the widget width, also capped by the
 * widget height so a very wide, very short widget cannot produce text taller than
 * its box. As with the grid, the result never exceeds that safe maximum.
 */
fun flowingTextMetrics(
    widthDp: Float,
    heightDp: Float,
    charCount: Int,
    fontScale: Float = 1.0f,
): GridMetrics {
    val width = saneSize(widthDp)
    val height = saneSize(heightDp)
    val chars = charCount.coerceAtLeast(1)

    val padding = (minOf(width, height) * 0.03f).coerceIn(2f, 12f)
    val usableWidth = (width - 2f * padding).coerceAtLeast(1f)
    val usableHeight = (height - 2f * padding).coerceAtLeast(1f)

    // ~1.7x the per-character slot keeps a proportional font inside the width.
    val maxFontFromWidth = (usableWidth / chars) * 1.7f
    val maxFontFromHeight = usableHeight * 0.5f
    val maxSafeFont = minOf(maxFontFromWidth, maxFontFromHeight)

    return GridMetrics(
        fontSize = safeFont(maxSafeFont, fontScale),
        cellWidth = usableWidth.dp,
        padding = padding.dp,
    )
}

/**
 * The font actually used: the user's preference applied to the safe maximum, but
 * clamped so it can never exceed [maxSafeFont].
 *
 * This is the fix for the extreme-size overflow: an earlier version applied a hard
 * floor (e.g. 4 sp) *after* computing the fit, which pushed the font back above the
 * size that fits and made a tiny widget overflow. Here the safe maximum is a hard
 * ceiling; a floor is only used when it is itself below that ceiling, so a
 * comfortable minimum is honoured on normal widgets without ever breaking the fit.
 */
private fun safeFont(maxSafeFont: Float, fontScale: Float): TextUnit {
    val ceiling = maxSafeFont.coerceIn(0f, MAX_FONT_SP)
    // Prefer at least MIN_FONT_SP for readability, but never above the fit.
    val preferred = maxOf(ceiling * FIT_HEADROOM, MIN_READABLE_FONT_SP).coerceAtMost(ceiling)
    val scaled = (preferred * saneScale(fontScale)).coerceAtMost(ceiling)
    return scaled.coerceAtLeast(0f).sp
}

/** Comfortable minimum on a normal widget; ignored when it would not fit. */
private const val MIN_READABLE_FONT_SP = 6f

/** Guards against a zero, negative or non-finite user scale. */
private fun saneScale(fontScale: Float): Float =
    fontScale.takeIf { it.isFinite() && it > 0f } ?: 1.0f

/**
 * A widget dimension that is safe to compute with: positive and finite.
 *
 * `Float.NaN.coerceAtLeast(1f)` stays `NaN`, so a non-finite size must be
 * replaced explicitly or the whole calculation becomes `NaN`.
 */
private fun saneSize(dp: Float): Float =
    dp.takeIf { it.isFinite() && it > 1f } ?: 1f
