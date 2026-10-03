package com.geronfir.wordclock.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the size -> typography mapping.
 *
 * `gridMetricsFor` / `flowingTextMetrics` are pure functions precisely so this can
 * run as a plain JVM test: no emulator, no instrumentation.
 *
 * The core guarantee — and the reason the old bucket approach was replaced — is
 * that the font never exceeds the size at which the content still fits the widget,
 * at *any* size the user drags it to (including the extremes). These tests assert
 * that directly against the fit bounds, not against remembered point sizes.
 */
class GridMetricsTest {

    /** The largest font that can fit one 5-column grid cell, per the production rule. */
    private fun gridFitCeiling(width: Float, height: Float): Float {
        val padding = (minOf(width, height) * 0.03f).coerceIn(2f, 12f)
        val usableWidth = (width - 2f * padding).coerceAtLeast(1f)
        val usableHeight = (height - 2f * padding).coerceAtLeast(1f)
        return minOf(usableWidth / 5f * 0.24f, usableHeight / 5f * 0.6f)
    }

    @Test
    fun `font size grows with widget size`() {
        val small = gridMetricsFor(100f, 100f).fontSize.value
        val medium = gridMetricsFor(160f, 160f).fontSize.value
        val large = gridMetricsFor(300f, 300f).fontSize.value
        assertTrue("$small < $medium", small < medium)
        assertTrue("$medium < $large", medium < large)
    }

    @Test
    fun `the grid font never exceeds the size that fits a cell`() {
        val sizes = listOf(20, 30, 60, 80, 110, 160, 220, 300, 500, 5000)
        sizes.forEach { s ->
            val m = gridMetricsFor(s.toFloat(), s.toFloat())
            val ceiling = gridFitCeiling(s.toFloat(), s.toFloat())
            assertTrue(
                "font ${m.fontSize.value} > fit ceiling $ceiling at $s",
                m.fontSize.value <= ceiling + 0.01f,
            )
        }
    }

    @Test
    fun `an extremely small widget shrinks the font instead of overflowing`() {
        // This is the regression the fix targets: no hard floor may push the font
        // back above the size that fits.
        val m = gridMetricsFor(30f, 30f)
        assertTrue("font must shrink", m.fontSize.value < 4f)
        assertTrue("still positive", m.fontSize.value > 0f)
    }

    @Test
    fun `an extremely large widget is capped so text cannot run away`() {
        val m = gridMetricsFor(5000f, 5000f)
        assertTrue("font <= ceiling", m.fontSize.value <= MAX_FONT_SP + 0.01f)
    }

    @Test
    fun `metrics use the smaller dimension so tall-narrow widgets stay readable`() {
        // A 110-wide, very tall widget is still limited by its width.
        val narrow = gridMetricsFor(110f, 400f)
        val square = gridMetricsFor(110f, 110f)
        assertEquals(square.fontSize.value, narrow.fontSize.value, 0.01f)
    }

    @Test
    fun `cell width and padding are always positive`() {
        listOf(40, 120, 160, 210, 280).forEach { s ->
            val m = gridMetricsFor(s.toFloat(), s.toFloat())
            assertTrue("cellWidth at $s", m.cellWidth.value > 0f)
            assertTrue("padding at $s", m.padding.value >= 0f)
        }
    }

    @Test
    fun `flowing text shrinks to fit a narrow widget`() {
        val narrow = flowingTextMetrics(80f, 110f, "JAM SETENGAH EMPAT".length)
        val wide = flowingTextMetrics(400f, 110f, "JAM SETENGAH EMPAT".length)
        assertTrue("narrow < wide", narrow.fontSize.value < wide.fontSize.value)
        assertTrue("narrow still positive", narrow.fontSize.value > 0f)
    }

    @Test
    fun `flowing text font never exceeds the width that fits the phrase`() {
        val chars = 20
        listOf(40, 80, 160, 320, 640).forEach { w ->
            val m = flowingTextMetrics(w.toFloat(), 120f, chars)
            val padding = (minOf(w.toFloat(), 120f) * 0.03f).coerceIn(2f, 12f)
            val ceiling = (w - 2f * padding) / chars * 1.7f
            assertTrue(
                "font ${m.fontSize.value} > fit ceiling $ceiling at width $w",
                m.fontSize.value <= ceiling + 0.01f,
            )
        }
    }

    @Test
    fun `flowing text is capped by the widget height`() {
        // Very wide but short: height must limit the font, not width.
        val short = flowingTextMetrics(600f, 40f, 5)
        assertTrue("font <= half height", short.fontSize.value <= 40f * 0.5f + 0.5f)
    }

    @Test
    fun `degenerate sizes never crash and stay within bounds`() {
        listOf(0f, -50f, Float.NaN, Float.POSITIVE_INFINITY).forEach { bad ->
            val m = gridMetricsFor(bad, bad)
            assertTrue("font finite at $bad", m.fontSize.value.isFinite())
            assertTrue("font non-negative at $bad", m.fontSize.value >= 0f)
            assertTrue("cell width positive at $bad", m.cellWidth.value > 0f)
        }
    }
}
