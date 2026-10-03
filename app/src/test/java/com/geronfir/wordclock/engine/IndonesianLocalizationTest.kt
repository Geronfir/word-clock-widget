package com.geronfir.wordclock.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the real Indonesian localization.
 *
 * These prove the second language is not a mere word swap: the phrase order is
 * Indonesian (hour first, *lebih*/*kurang*, and the half hour read as *setengah*
 * toward the next hour), while the engine output it consumes is unchanged.
 */
class IndonesianLocalizationTest {

    private val engine = TimeExpressionEngine()
    private val localization = IndonesianTimeLocalization

    private fun phrase(hour: Int, minute: Int): String =
        localization.format(engine.expressionAt(hour, minute))

    @Test
    fun `on the hour is jam plus the hour`() {
        assertEquals("JAM TIGA", phrase(3, 0))
    }

    @Test
    fun `minutes past use lebih`() {
        assertEquals("JAM TIGA LEBIH SEPULUH", phrase(3, 10))
        assertEquals("JAM TIGA LEBIH SEPEREMPAT", phrase(3, 15))
        assertEquals("JAM TIGA LEBIH DUA PULUH", phrase(3, 20))
        assertEquals("JAM TIGA LEBIH DUA PULUH LIMA", phrase(3, 25))
    }

    @Test
    fun `half hour reads as setengah toward the next hour`() {
        // The classic trap: "setengah empat" is 03:30, not 04:30.
        assertEquals("JAM SETENGAH EMPAT", phrase(3, 30))
        assertEquals("JAM SETENGAH SATU", phrase(12, 30))
    }

    @Test
    fun `minutes to use kurang and the next hour`() {
        assertEquals("JAM EMPAT KURANG DUA PULUH", phrase(3, 40))
        assertEquals("JAM EMPAT KURANG SEPEREMPAT", phrase(3, 45))
        assertEquals("JAM EMPAT KURANG LIMA", phrase(3, 55))
    }

    @Test
    fun `midnight and noon read duabelas`() {
        assertEquals("JAM DUABELAS", phrase(0, 0))
        assertEquals("JAM DUABELAS", phrase(12, 0))
    }

    @Test
    fun `day period is appended when requested`() {
        val morning = TimeExpressionEngine(TimeConfig(includeDayPeriod = true))
            .expressionAt(3, 0)
        assertEquals("JAM TIGA PAGI", localization.format(morning))
    }

    @Test
    fun `every minute of the day produces a valid indonesian phrase`() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val phrase = phrase(hour, minute)
                assertTrue("empty phrase at $hour:$minute", phrase.isNotBlank())
                assertTrue("missing JAM at $hour:$minute", phrase.startsWith("JAM"))
            }
        }
    }

    @Test
    fun `registry exposes indonesian as fully supported`() {
        assertTrue(LocalizationRegistry.isFullySupported("id"))
        assertEquals(IndonesianVocabulary, LocalizationRegistry.vocabulary("id"))
        assertEquals(IndonesianWordGrid.grid, LocalizationRegistry.grid("id"))
        assertEquals(IndonesianTimeLocalization, LocalizationRegistry.localization("id"))
        assertTrue(LocalizationRegistry.selectableLanguageTags.contains("id"))
    }
}
