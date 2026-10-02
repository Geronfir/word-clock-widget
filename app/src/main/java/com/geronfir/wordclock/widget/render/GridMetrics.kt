package com.geronfir.wordclock.widget.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalSize

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
 */
data class GridMetrics(
    val fontSize: TextUnit,
    val cellWidth: androidx.compose.ui.unit.Dp,
    val padding: androidx.compose.ui.unit.Dp,
    val horizontalSpacing: androidx.compose.ui.unit.Dp,
)

/** Pure function so it can be unit-tested without an Android runtime. */
fun gridMetricsFor(size: DpSize): GridMetrics {
    val smallest = minOf(size.width.value, size.height.value)
    return when {
        smallest >= 250f -> GridMetrics(16.sp, 88.dp, 12.dp, 8.dp)
        smallest >= 200f -> GridMetrics(14.sp, 76.dp, 10.dp, 6.dp)
        smallest >= 150f -> GridMetrics(12.sp, 62.dp, 8.dp, 5.dp)
        smallest >= 110f -> GridMetrics(10.sp, 52.dp, 6.dp, 4.dp)
        else -> GridMetrics(8.sp, 42.dp, 4.dp, 2.dp)
    }
}

/** The metrics for the widget currently being composed. */
@Composable
fun currentGridMetrics(): GridMetrics = gridMetricsFor(LocalSize.current)
