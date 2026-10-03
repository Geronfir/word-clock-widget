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
 * The important guarantee, and the reason the old bucket approach was replaced, is
 * that the rendered content always fits the widget at *any* size the user drags it
 * to — including the extremes.
 */
class GridMetricsTest {

    @Test
    fun `font size grows with widget size`() {
        val small = gridMetricsFor(100f, 100f).fontSize.value
        val medium = gridMetricsFor(160f, 160f).fontSize.value
        val large = gridMetricsFor(300f, 300f).fontSize.value
        assertTrue("$small < $medium", small < medium)
        assertTrue("$medium < $large", medium < large)
    }

    @Test
    fun `the grid always fits inside the widget width`() {
        val sizes = listOf(40, 60, 80, 110, 160, 220, 300, 500)
        sizes.forEach { s ->
            val m = gridMetricsFor(s.toFloat(), s.toFloat())
            val rowWidth = m.cellWidth.value * 5
            assertTrue("row width $rowWidth > widget $s", rowWidth <= s.toFloat() + 0.5f)
        }
    }

    @Test
    fun `an extremely small widget still yields a readable, non-overflowing grid`() {
        val m = gridMetricsFor(30f, 30f)
        assertTrue("font >= floor", m.fontSize.value >= MIN_FONT_SP - 0.01f)
        assertTrue("cell width positive", m.cellWidth.value > 0f)
        assertTrue("row fits", m.cellWidth.value * 5 <= 30f + 0.5f)
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
        assertTrue("narrow still readable", narrow.fontSize.value >= MIN_FONT_SP - 0.01f)
    }

    @Test
    fun `flowing text is capped by the widget height`() {
        // Very wide but short: height must limit the font, not width.
        val short = flowingTextMetrics(600f, 40f, 5)
        assertTrue("font <= half height", short.fontSize.value <= 40f * 0.5f + 0.5f)
    }

    @Test
    fun `degenerate sizes never crash and stay within bounds`() {
        listOf(0f, -50f, Float.NaN).forEach { bad ->
            val m = gridMetricsFor(bad, bad)
            assertTrue("font in range at $bad", m.fontSize.value in MIN_FONT_SP..MAX_FONT_SP)
            assertTrue("cell width positive at $bad", m.cellWidth.value > 0f)
        }
    }
}
