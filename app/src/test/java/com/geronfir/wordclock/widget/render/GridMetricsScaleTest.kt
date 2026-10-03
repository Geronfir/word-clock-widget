package com.geronfir.wordclock.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the user's font-size preference.
 *
 * `gridMetricsFor` is a pure function precisely so this can run as a plain JVM
 * test: no emulator, no instrumentation.
 *
 * The preference scales the font but is always capped by the size that fits, so a
 * "larger" setting can never push the text outside the widget.
 */
class GridMetricsScaleTest {

    @Test
    fun `default scale leaves the size unchanged`() {
        val base = gridMetricsFor(200f, 200f)
        val withOne = gridMetricsFor(200f, 200f, 1.0f)
        assertEquals(base.fontSize.value, withOne.fontSize.value, 0.01f)
    }

    @Test
    fun `a larger scale never shrinks the font`() {
        val normal = gridMetricsFor(200f, 200f, 1.0f).fontSize.value
        val large = gridMetricsFor(200f, 200f, 1.5f).fontSize.value
        assertTrue("$normal <= $large", normal <= large + 0.01f)
    }

    @Test
    fun `a smaller scale never grows the font`() {
        val normal = gridMetricsFor(200f, 200f, 1.0f).fontSize.value
        val small = gridMetricsFor(200f, 200f, 0.75f).fontSize.value
        assertTrue("$small <= $normal", small <= normal + 0.01f)
    }

    @Test
    fun `scale never pushes the font past the fit ceiling`() {
        // Even at the largest offered preference, the font stays within the size
        // that fits a 5-column grid.
        listOf(80f, 120f, 200f, 320f).forEach { s ->
            val m = gridMetricsFor(s, s, 1.5f)
            val padding = (s * 0.03f).coerceIn(2f, 12f)
            val ceiling = ((s - 2f * padding) / 5f) * 0.24f
            assertTrue("font ${m.fontSize.value} > ceiling $ceiling at $s", m.fontSize.value <= ceiling + 0.01f)
        }
    }

    @Test
    fun `scale does not change cell width or padding`() {
        val normal = gridMetricsFor(200f, 200f, 1.0f)
        val large = gridMetricsFor(200f, 200f, 1.5f)
        assertEquals(normal.cellWidth.value, large.cellWidth.value, 0.01f)
        assertEquals(normal.padding.value, large.padding.value, 0.01f)
    }

    @Test
    fun `non-positive or non-finite scale falls back to one`() {
        val base = gridMetricsFor(200f, 200f, 1.0f).fontSize.value
        assertEquals(base, gridMetricsFor(200f, 200f, 0f).fontSize.value, 0.01f)
        assertEquals(base, gridMetricsFor(200f, 200f, -2f).fontSize.value, 0.01f)
        assertEquals(base, gridMetricsFor(200f, 200f, Float.NaN).fontSize.value, 0.01f)
    }

    @Test
    fun `every offered scale keeps the font positive and finite`() {
        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { s ->
            val m = gridMetricsFor(120f, 120f, s)
            assertTrue("font at scale $s", m.fontSize.value.isFinite() && m.fontSize.value > 0f)
        }
    }
}
