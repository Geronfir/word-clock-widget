package com.geronfir.wordclock.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Edge cases the spec calls out explicitly: minute/hour rollovers, midnight,
 * noon, day boundaries, timezone handling and graceful failure.
 */
class EdgeCaseTest {

    private val engine = TimeExpressionEngine()
    private val formatter = PhraseFormatter(EnglishVocabulary)

    @Test
    fun `every minute of the day produces a valid phrase`() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val t = engine.expressionAt(hour, minute)
                val phrase = formatter.format(t)
                assertTrue("empty phrase at $hour:$minute", phrase.isNotBlank())
                assertTrue("missing IT IS at $hour:$minute", phrase.startsWith("IT IS"))
                assertTrue("hour out of range at $hour:$minute", t.phraseHour12 in 1..12)
            }
        }
    }

    @Test
    fun `every minute of the day lights at least one grid cell`() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val t = engine.expressionAt(hour, minute)
                val cells = EnglishWordGrid.grid.activeCells(t.activeWords)
                assertTrue("no active cell at $hour:$minute", cells.isNotEmpty())
            }
        }
    }

    @Test
    fun `midnight and noon both read twelve o'clock`() {
        assertEquals("IT IS TWELVE O'CLOCK", formatter.format(engine.expressionAt(0, 0)))
        assertEquals("IT IS TWELVE O'CLOCK", formatter.format(engine.expressionAt(12, 0)))
    }

    @Test
    fun `day boundary at 23h59 rolls to midnight`() {
        val t = engine.expressionAt(23, 59)
        assertEquals(0, t.roundedMinute)
        assertEquals(12, t.phraseHour12)
        assertEquals("IT IS TWELVE O'CLOCK", formatter.format(t))
    }

    @Test
    fun `minute rollover at 59 carries the hour`() {
        // 07:59 rounds to 08:00
        val t = engine.expressionAt(7, 59)
        assertEquals("IT IS EIGHT O'CLOCK", formatter.format(t))
    }

    @Test
    fun `engine handles a DST spring-forward instant without crashing`() {
        // Europe/London springs forward at 01:00; the wall clock skips an hour.
        val zone = ZoneId.of("Europe/London")
        val clock = java.time.Clock.fixed(
            java.time.Instant.parse("2026-03-29T01:30:00Z"),
            zone,
        )
        val t = TimeExpressionEngine().expressionNow(zone, clock)
        assertTrue(t.hour24 in 0..23)
        assertTrue(formatter.format(t).startsWith("IT IS"))
    }

    @Test
    fun `engine handles a DST fall-back instant without crashing`() {
        val zone = ZoneId.of("Europe/London")
        val clock = java.time.Clock.fixed(
            java.time.Instant.parse("2026-10-25T01:30:00Z"),
            zone,
        )
        val t = TimeExpressionEngine().expressionNow(zone, clock)
        assertTrue(t.hour24 in 0..23)
    }

    @Test
    fun `a far-future leap-day instant is handled`() {
        val zone = ZoneId.of("UTC")
        val clock = java.time.Clock.fixed(
            java.time.Instant.parse("2028-02-29T13:45:00Z"),
            zone,
        )
        val t = TimeExpressionEngine().expressionNow(zone, clock)
        assertEquals("IT IS QUARTER TO TWO", formatter.format(t))
    }

    @Test
    fun `timezone changes alter the rendered hour`() {
        val instant = java.time.Instant.parse("2026-06-01T12:00:00Z")
        val utc = TimeExpressionEngine().expressionNow(
            ZoneId.of("UTC"),
            java.time.Clock.fixed(instant, ZoneId.of("UTC")),
        )
        val tokyo = TimeExpressionEngine().expressionNow(
            ZoneId.of("Asia/Tokyo"),
            java.time.Clock.fixed(instant, ZoneId.of("Asia/Tokyo")),
        )
        // 12:00 UTC == 21:00 in Tokyo.
        assertEquals(12, utc.hour24)
        assertEquals(21, tokyo.hour24)
    }

    @Test
    fun `rounding can be turned off for exact minutes`() {
        val exact = TimeExpressionEngine(TimeConfig(roundToNearestFive = false))
        val t = exact.expressionAt(3, 7)
        assertEquals(7, t.roundedMinute)
        assertEquals(MinuteUnit.NONE, t.minuteUnit)
    }

    @Test
    fun `invalid times are rejected rather than silently mis-rendered`() {
        assertThrows { engine.expressionAt(-1, 0) }
        assertThrows { engine.expressionAt(24, 0) }
        assertThrows { engine.expressionAt(0, 60) }
        assertThrows { engine.expressionAt(0, -5) }
    }

    @Test
    fun `grid active cells never outnumber active words`() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val t = engine.expressionAt(hour, minute)
                val cells = EnglishWordGrid.grid.activeCells(t.activeWords)
                assertFalse(cells.isEmpty())
                assertTrue(cells.size <= t.activeWords.size)
            }
        }
    }

    private inline fun assertThrows(block: () -> Unit) {
        try {
            block()
            throw AssertionError("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
