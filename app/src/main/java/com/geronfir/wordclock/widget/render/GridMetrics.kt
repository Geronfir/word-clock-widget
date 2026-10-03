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
 * they broke at the extremes: a small widget stayed on a bucket whose 5-column
 * row was wider than the widget (the text overflowed its parent), and a large one
 * could snap to a size that no longer matched the box.
 *
 * Everything here is a pure function of the widget size, so it can be unit-tested
 * on a plain JVM and never has to guess.
 */
data class GridMetrics(
    val fontSize: TextUnit,
    val cellWidth: Dp,
    val padding: Dp,
)

/** Hard floor/ceiling so a degenerate size can never produce unreadable text. */
internal const val MIN_FONT_SP = 4f
internal const val MAX_FONT_SP = 48f

/**
 * Metrics for the word-grid style.
 *
 * The grid is [columns] x [rows] cells; every row must fit inside the widget, so
 * the cell width is derived from the width (minus padding), and the font is the
 * smaller of "fits a cell's width" and "fits a row's height". That guarantees the
 * matrix never spills outside the widget at any size the user drags it to.
 *
 * @param fontScale the user's font-size preference (1.0 = as computed).
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
    // cell width keeps the longest word inside its cell.
    val fontFromWidth = cellWidth * 0.24f
    val fontFromHeight = (usableHeight / rowCount) * 0.6f

    val fontSize = minOf(fontFromWidth, fontFromHeight)
        .coerceIn(MIN_FONT_SP, MAX_FONT_SP) * saneScale(fontScale)

    return GridMetrics(
        fontSize = fontSize.coerceIn(MIN_FONT_SP, MAX_FONT_SP).sp,
        cellWidth = cellWidth.dp,
        padding = padding.dp,
    )
}

/**
 * Metrics for the flowing-text style.
 *
 * There is no grid here, just one phrase. The font is chosen so [charCount]
 * characters fit across the widget width, and is also capped by the widget height
 * so a very wide, very short widget cannot produce text taller than its box.
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
    val fontFromWidth = (usableWidth / chars) * 1.7f
    val fontFromHeight = usableHeight * 0.5f

    val fontSize = minOf(fontFromWidth, fontFromHeight)
        .coerceIn(MIN_FONT_SP, MAX_FONT_SP) * saneScale(fontScale)

    return GridMetrics(
        fontSize = fontSize.coerceIn(MIN_FONT_SP, MAX_FONT_SP).sp,
        cellWidth = usableWidth.dp,
        padding = padding.dp,
    )
}

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
