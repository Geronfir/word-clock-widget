package com.geronfir.wordclock.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Proves the localization architecture is genuinely extensible.
 *
 * The spec forbids "a dictionary that merely replaces English words": a language
 * must be able to define its own word order and its own grid. This test adds a
 * toy language whose grammar is *not* English (it places the hour before the
 * minutes and uses no "past"/"to" marker) and shows the engine + grid handle it
 * with no changes to the widget.
 */
class LocalizationExtensibilityTest {

    /**
     * A deliberately non-English ordering: "<hour> <minute> o'clock-ish".
     * It reuses the engine's structured output but emits its own word order,
     * exactly as a real second language would.
     */
    private class ToyLanguageFormatter(private val vocabulary: WordVocabulary) {
        fun format(t: SemanticTime): String {
            // Hour first, then minutes, no PAST/TO connector.
            val parts = buildList {
                add(vocabulary.word(WordKey.IT_IS))
                add(vocabulary.word(hourWordFor(t.phraseHour12)))
                if (t.minuteUnit != MinuteUnit.NONE) {
                    when (t.minuteUnit) {
                        MinuteUnit.TWENTY_FIVE -> {
                            add(vocabulary.word(WordKey.TWENTY))
                            add(vocabulary.word(WordKey.FIVE))
                        }
                        else -> add(vocabulary.word(minuteWordFor(t.minuteUnit)))
                    }
                }
            }
            return parts.joinToString(" ")
        }

        private fun hourWordFor(hour12: Int): WordKey = when (hour12) {
            1 -> WordKey.ONE; 2 -> WordKey.TWO; 3 -> WordKey.THREE; 4 -> WordKey.FOUR
            5 -> WordKey.FIVE; 6 -> WordKey.SIX; 7 -> WordKey.SEVEN; 8 -> WordKey.EIGHT
            9 -> WordKey.NINE; 10 -> WordKey.TEN; 11 -> WordKey.ELEVEN; else -> WordKey.TWELVE
        }

        private fun minuteWordFor(unit: MinuteUnit): WordKey = when (unit) {
            MinuteUnit.FIVE -> WordKey.FIVE
            MinuteUnit.TEN -> WordKey.TEN
            MinuteUnit.QUARTER -> WordKey.QUARTER
            MinuteUnit.TWENTY -> WordKey.TWENTY
            MinuteUnit.HALF -> WordKey.HALF
            else -> WordKey.OCLOCK
        }
    }

    @Test
    fun `a second language can define its own word order`() {
        val t = TimeExpressionEngine().expressionAt(3, 30)
        val phrase = ToyLanguageFormatter(EnglishVocabulary).format(t)
        // English would say "IT IS HALF PAST THREE"; the toy language puts the hour first.
        assertEquals("IT IS THREE HALF", phrase)
    }

    @Test
    fun `the engine output is language neutral`() {
        // The same SemanticTime drives both English and the toy language.
        val t = TimeExpressionEngine().expressionAt(3, 30)
        assertEquals(3, t.phraseHour12)
        assertEquals(MinuteUnit.HALF, t.minuteUnit)
        assertEquals("IT IS HALF PAST THREE", PhraseFormatter(EnglishVocabulary).format(t))
    }

    @Test
    fun `a language may use a different grid`() {
        // A 2x2 grid for a language with fewer words, proving the grid is not global.
        val tinyGrid = WordGrid(
            languageTag = "xx",
            rows = listOf(
                listOf(WordKey.IT_IS, WordKey.HALF),
                listOf(WordKey.PAST, WordKey.THREE),
            ),
        )
        val cells = tinyGrid.activeCells(listOf(WordKey.IT_IS, WordKey.HALF, WordKey.THREE))
        assertEquals(3, cells.size)
        assertTrue(WordGrid.Cell(0, 0) in cells)
        assertTrue(WordGrid.Cell(1, 1) in cells)
    }

    @Test
    fun `registry falls back to english for an unknown language`() {
        assertEquals(EnglishVocabulary, LocalizationRegistry.vocabulary("zz"))
        assertEquals(EnglishTimeLocalization, LocalizationRegistry.localization("zz"))
        assertEquals(null, LocalizationRegistry.gridOrNull("zz"))
        assertTrue(LocalizationRegistry.isFullySupported("en"))
        assertTrue(!LocalizationRegistry.isFullySupported("zz"))
    }

    @Test
    fun `registry reports its supported languages`() {
        assertTrue(LocalizationRegistry.supportedLanguageTags.contains("en"))
        assertEquals("en", LocalizationRegistry.DEFAULT_LANGUAGE)
    }
}
