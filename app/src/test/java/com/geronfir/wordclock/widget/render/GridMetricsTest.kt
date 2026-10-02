package com.geronfir.wordclock.widget.render

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
        val m = gridMetricsFor(100f, 90f)
        assertEquals(8f, m.fontSize.value, 0.01f)
    }

    @Test
    fun `large widget uses the largest bucket`() {
        val m = gridMetricsFor(300f, 260f)
        assertEquals(16f, m.fontSize.value, 0.01f)
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
    fun `metrics use the smaller dimension so tall-narrow widgets stay readable`() {
        // Width 110 -> the 110..149 bucket, even though height is huge.
        val narrow = gridMetricsFor(110f, 400f)
        assertEquals(10f, narrow.fontSize.value, 0.01f)

        // A 110-wide, 110-tall widget lands in the same bucket.
        val square = gridMetricsFor(110f, 110f)
        assertEquals(square.fontSize.value, narrow.fontSize.value, 0.01f)
    }

    @Test
    fun `every bucket has positive cell width and padding`() {
        val sizes = listOf(80, 120, 160, 210, 280)
        sizes.forEach { s ->
            val m = gridMetricsFor(s.toFloat(), s.toFloat())
            assertTrue("cellWidth at $s", m.cellWidth.value > 0f)
            assertTrue("padding at $s", m.padding.value >= 0f)
        }
    }
}
