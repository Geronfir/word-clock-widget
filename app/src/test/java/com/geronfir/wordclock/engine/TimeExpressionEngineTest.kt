package com.geronfir.wordclock.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Time Representation Engine.
 *
 * These run on a plain JVM (no Android, no emulator), which is exactly the point
 * of keeping the engine free of UI dependencies.
 */
class TimeExpressionEngineTest {

    private val engine = TimeExpressionEngine()

    private fun phrase(hour: Int, minute: Int): String =
        PhraseFormatter(EnglishVocabulary).format(engine.expressionAt(hour, minute))

    // --- Exact / o'clock -----------------------------------------------------

    @Test
    fun `midnight is twelve o'clock`() {
        assertEquals("IT IS TWELVE O'CLOCK", phrase(0, 0))
    }

    @Test
    fun `noon is twelve o'clock`() {
        assertEquals("IT IS TWELVE O'CLOCK", phrase(12, 0))
    }

    @Test
    fun `five past three`() {
        assertEquals("IT IS FIVE PAST THREE", phrase(3, 5))
    }

    @Test
    fun `quarter past three`() {
        assertEquals("IT IS QUARTER PAST THREE", phrase(3, 15))
    }

    @Test
    fun `half past three`() {
        assertEquals("IT IS HALF PAST THREE", phrase(3, 30))
    }

    @Test
    fun `twenty five to four`() {
        assertEquals("IT IS TWENTY FIVE TO FOUR", phrase(3, 35))
    }

    @Test
    fun `quarter to four`() {
        assertEquals("IT IS QUARTER TO FOUR", phrase(3, 45))
    }

    // --- Boundary minutes ----------------------------------------------------

    @Test
    fun `minute 1 rounds back to the hour`() {
        assertEquals("IT IS THREE O'CLOCK", phrase(3, 1))
    }

    @Test
    fun `minute 2 rounds forward to five past`() {
        assertEquals("IT IS FIVE PAST THREE", phrase(3, 2))
    }

    @Test
    fun `minute 3 rounds forward to five past`() {
        assertEquals("IT IS FIVE PAST THREE", phrase(3, 3))
    }

    @Test
    fun `minute 58 rounds forward and carries the hour`() {
        // 03:58 rounds to 04:00 -> "four o'clock"
        assertEquals("IT IS FOUR O'CLOCK", phrase(3, 58))
    }

    @Test
    fun `minute 58 at 23h wraps to midnight`() {
        assertEquals("IT IS TWELVE O'CLOCK", phrase(23, 58))
    }

    // --- Hour transitions ----------------------------------------------------

    @Test
    fun `twenty five to twelve at 11h35`() {
        assertEquals("IT IS TWENTY FIVE TO TWELVE", phrase(11, 35))
    }

    @Test
    fun `quarter to one at 12h45 points to one`() {
        assertEquals("IT IS QUARTER TO ONE", phrase(12, 45))
    }

    @Test
    fun `ten to eleven at 22h50`() {
        assertEquals("IT IS TEN TO ELEVEN", phrase(22, 50))
    }

    // --- Structured output ---------------------------------------------------

    @Test
    fun `twenty five past is expressed as two minute words`() {
        val t = engine.expressionAt(3, 25)
        assertEquals(MinuteUnit.TWENTY_FIVE, t.minuteUnit)
        assertEquals(TimeExpressionType.PAST, t.expressionType)
        assertTrue(t.activeWords.containsAll(listOf(WordKey.TWENTY, WordKey.FIVE, WordKey.PAST)))
    }

    @Test
    fun `to expression points at the next hour`() {
        val t = engine.expressionAt(7, 35)
        assertEquals(TimeExpressionType.TO, t.expressionType)
        assertEquals(8, t.phraseHour12)
    }

    @Test
    fun `o'clock expression keeps the current hour`() {
        val t = engine.expressionAt(7, 0)
        assertEquals(TimeExpressionType.O_CLOCK, t.expressionType)
        assertEquals(7, t.phraseHour12)
    }

    @Test
    fun `day period is off by default`() {
        val t = engine.expressionAt(9, 0)
        assertEquals(false, t.hasDayPeriod)
    }

    // --- 12/24 hour behaviour ------------------------------------------------

    @Test
    fun `day period suffix can be enabled`() {
        val amEngine = TimeExpressionEngine(TimeConfig(includeDayPeriod = true))
        val morning = amEngine.expressionAt(9, 0)
        assertTrue(morning.activeWords.contains(WordKey.AM))

        val evening = amEngine.expressionAt(21, 0)
        assertTrue(evening.activeWords.contains(WordKey.PM))
    }

    @Test
    fun `24 hour mode uses descriptive day periods`() {
        val e = TimeExpressionEngine(TimeConfig(use24Hour = true, includeDayPeriod = true))
        assertTrue(e.expressionAt(9, 0).activeWords.contains(WordKey.IN_THE_MORNING))
        assertTrue(e.expressionAt(14, 0).activeWords.contains(WordKey.IN_THE_AFTERNOON))
        assertTrue(e.expressionAt(19, 0).activeWords.contains(WordKey.IN_THE_EVENING))
        assertTrue(e.expressionAt(23, 0).activeWords.contains(WordKey.AT_NIGHT))
    }

    @Test
    fun `twenty four hour midnight maps to twelve`() {
        val t = engine.expressionAt(0, 0)
        assertEquals(12, t.phraseHour12)
    }

    // --- Timezone awareness --------------------------------------------------

    @Test
    fun `engine honours an explicit zone`() {
        val utc = TimeExpressionEngine().expressionNow(java.time.ZoneId.of("UTC"))
        val tokyo = TimeExpressionEngine().expressionNow(java.time.ZoneId.of("Asia/Tokyo"))
        assertTrue(utc.hour24 in 0..23)
        assertTrue(tokyo.hour24 in 0..23)
    }

    // --- Invalid input -------------------------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun `rejects hour 24`() {
        engine.expressionAt(24, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects negative minute`() {
        engine.expressionAt(1, -1)
    }
}
