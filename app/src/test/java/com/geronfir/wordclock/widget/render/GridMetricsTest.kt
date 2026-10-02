package com.geronfir.wordclock.widget.render

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the size -> typography mapping.
 *
 * `gridMetricsFor` is a pure function precisely so this can run as a plain JVM
 * test: no emulator, no instrumentation.
 */
class GridMetricsTest {

    @Test
    fun `tiny widget uses the smallest bucket`() {
        val m = gridMetricsFor(DpSize(100.dp, 90.dp))
        assertEquals(8f, m.fontSize.value, 0.01f)
    }

    @Test
    fun `large widget uses the largest bucket`() {
        val m = gridMetricsFor(DpSize(300.dp, 260.dp))
        assertEquals(16f, m.fontSize.value, 0.01f)
    }

    @Test
    fun `font size grows with widget size`() {
        val small = gridMetricsFor(DpSize(100.dp, 100.dp)).fontSize.value
        val medium = gridMetricsFor(DpSize(160.dp, 160.dp)).fontSize.value
        val large = gridMetricsFor(DpSize(300.dp, 300.dp)).fontSize.value
        assertTrue("$small < $medium", small < medium)
        assertTrue("$medium < $large", medium < large)
    }

    @Test
    fun `metrics use the smaller dimension so tall-narrow widgets stay readable`() {
        val narrow = gridMetricsFor(DpSize(110.dp, 400.dp))
        val tiny = gridMetricsFor(DpSize(100.dp, 100.dp))
        assertEquals(tiny.fontSize.value, narrow.fontSize.value, 0.01f)
    }

    @Test
    fun `every bucket has positive cell width and padding`() {
        val sizes = listOf(80, 120, 160, 210, 280)
        sizes.forEach { s ->
            val m = gridMetricsFor(DpSize(s.dp, s.dp))
            assertTrue("cellWidth at $s", m.cellWidth.value > 0f)
            assertTrue("padding at $s", m.padding.value >= 0f)
        }
    }
}
