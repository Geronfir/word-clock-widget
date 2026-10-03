package com.geronfir.wordclock.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the user's font-size preference.
 *
 * `gridMetricsFor` is a pure function precisely so this can run as a plain JVM
 * test: no emulator, no instrumentation.
 */
class GridMetricsScaleTest {

    @Test
    fun `default scale leaves the size unchanged`() {
        val base = gridMetricsFor(160f, 160f)
        val withOne = gridMetricsFor(160f, 160f, 1.0f)
        assertEquals(base.fontSize.value, withOne.fontSize.value, 0.01f)
    }

    @Test
    fun `scale multiplies the font size`() {
        val normal = gridMetricsFor(160f, 160f, 1.0f).fontSize.value
        val large = gridMetricsFor(160f, 160f, 1.5f).fontSize.value
        assertEquals(normal * 1.5f, large, 0.01f)
    }

    @Test
    fun `scale does not change cell width or padding`() {
        val normal = gridMetricsFor(160f, 160f, 1.0f)
        val large = gridMetricsFor(160f, 160f, 1.5f)
        assertEquals(normal.cellWidth.value, large.cellWidth.value, 0.01f)
        assertEquals(normal.padding.value, large.padding.value, 0.01f)
    }

    @Test
    fun `non-positive or non-finite scale falls back to one`() {
        val base = gridMetricsFor(160f, 160f, 1.0f).fontSize.value
        assertEquals(base, gridMetricsFor(160f, 160f, 0f).fontSize.value, 0.01f)
        assertEquals(base, gridMetricsFor(160f, 160f, -2f).fontSize.value, 0.01f)
        assertEquals(base, gridMetricsFor(160f, 160f, Float.NaN).fontSize.value, 0.01f)
    }

    @Test
    fun `every offered scale keeps the font positive`() {
        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { s ->
            val m = gridMetricsFor(120f, 120f, s)
            assertTrue("font at scale $s", m.fontSize.value > 0f)
        }
    }
}
