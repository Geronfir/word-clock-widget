package com.geronfir.wordclock.widget.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
    val cellWidth: Dp,
    val padding: Dp,
)

/**
 * Pure function so it can be unit-tested without an Android runtime.
 *
 * Takes plain floats rather than `DpSize`: `DpSize` is a value class, and passing
 * it as a parameter to a function called from a composable that reads
 * `LocalSize.current` crashes the Kotlin/JVM IR backend ("Couldn't inline method
 * call"). Floats keep the compiler happy and the function equally testable.
 */
fun gridMetricsFor(widthDp: Float, heightDp: Float): GridMetrics {
    val smallest = minOf(widthDp, heightDp)
    return when {
        smallest >= 250f -> GridMetrics(16.sp, 88.dp, 12.dp)
        smallest >= 200f -> GridMetrics(14.sp, 76.dp, 10.dp)
        smallest >= 150f -> GridMetrics(12.sp, 62.dp, 8.dp)
        smallest >= 110f -> GridMetrics(10.sp, 52.dp, 6.dp)
        else -> GridMetrics(8.sp, 42.dp, 4.dp)
    }
}

/** The metrics for the widget currently being composed. */
@Composable
fun currentGridMetrics(): GridMetrics {
    val width = LocalSize.current.width.value
    val height = LocalSize.current.height.value
    return gridMetricsFor(width, height)
}
