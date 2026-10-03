package com.geronfir.wordclock.widget.render

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Maps the current widget size to typography and padding.
 *
 * A word clock is placed at many sizes, from a small 2x1 tile to a wide 4x3
 * board. Rather than one hard-coded font size, the grid scales with the space the
 * user actually gave it.
 *
 * The scaling is intentionally simple (a handful of buckets) so it stays cheap:
 * this runs on every minute tick, and the widget must not do layout maths it
 * cannot afford.
 *
 * Sizes arrive as plain dp floats measured from Glance's `LocalSize` inside the
 * composition (see `WordClockWidget`). `LocalSize` is only ever read and
 * immediately converted to `Float`; the `DpSize` value class is never passed
 * across a composable boundary, which is what used to crash the Kotlin IR
 * backend with "Couldn't inline method call: CompositionLocal.get-current".
 */
data class GridMetrics(
    val fontSize: TextUnit,
    val cellWidth: Dp,
    val padding: Dp,
)

/** Pure function so it can be unit-tested without an Android runtime. */
fun gridMetricsFor(widthDp: Float, heightDp: Float, fontScale: Float = 1.0f): GridMetrics {
    val smallest = minOf(widthDp, heightDp)
    val base = when {
        smallest >= 250f -> GridMetrics(16.sp, 88.dp, 12.dp)
        smallest >= 200f -> GridMetrics(14.sp, 76.dp, 10.dp)
        smallest >= 150f -> GridMetrics(12.sp, 62.dp, 8.dp)
        smallest >= 110f -> GridMetrics(10.sp, 52.dp, 6.dp)
        else -> GridMetrics(8.sp, 42.dp, 4.dp)
    }
    // The user's font-size preference scales only the text; cell widths and
    // padding stay tied to the widget size so the grid keeps its alignment.
    val scale = fontScale.takeIf { it.isFinite() && it > 0f } ?: 1.0f
    return base.copy(fontSize = (base.fontSize.value * scale).sp)
}
