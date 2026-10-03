package com.geronfir.wordclock.widget

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the raw AppWidgetManager option -> dp conversion.
 *
 * `widgetSizeInDp` is a pure function precisely so this can run as a plain JVM
 * test: no emulator, no instrumentation.
 */
class WidgetSizeResolverTest {

    @Test
    fun `dp options are passed through unchanged`() {
        val (widthDp, heightDp) = widgetSizeInDp(180, 110)
        assertEquals(180f, widthDp, 0.01f)
        assertEquals(110f, heightDp, 0.01f)
    }

    @Test
    fun `a large widget keeps its dp value`() {
        val (widthDp, _) = widgetSizeInDp(280, 260)
        assertEquals(280f, widthDp, 0.01f)
    }

    @Test
    fun `non-positive options fall back to the provider minimum`() {
        assertEquals(FALLBACK_WIDGET_DP to FALLBACK_WIDGET_DP, widgetSizeInDp(0, 0))
        assertEquals(FALLBACK_WIDGET_DP to FALLBACK_WIDGET_DP, widgetSizeInDp(180, 0))
        assertEquals(FALLBACK_WIDGET_DP to FALLBACK_WIDGET_DP, widgetSizeInDp(-1, 110))
    }
}
