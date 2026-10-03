package com.geronfir.wordclock.engine

/**
 * Turns a [SemanticTime] into the words of *one language*.
 *
 * A localization produces a readable phrase. It also optionally produces the
 * [WordKey]s to light in a word grid — but only for languages that *have* a grid
 * (see [LocalizationRegistry.gridOrNull]). A word grid is a fixed physical
 * matrix read left-to-right, top-to-bottom; a language whose phrase order varies
 * by case cannot be expressed by one matrix, so it returns flowing text instead
 * and never lights a grid.
 */
interface TimeLocalization {
    val languageTag: String

    /** The readable phrase, in this language's own word order. */
    fun format(time: SemanticTime): String

    /** The words to light in this language's grid, or `null` if it has no grid. */
    fun gridWords(time: SemanticTime): List<WordKey>?
}

/**
 * English: the engine's word list is already English-shaped, so the grid words
 * are exactly what the engine produced ("IT IS <minutes> PAST/TO <hour>").
 */
object EnglishTimeLocalization : TimeLocalization {
    override val languageTag: String = "en"

    override fun format(time: SemanticTime): String =
        PhraseFormatter(EnglishVocabulary).format(time)

    override fun gridWords(time: SemanticTime): List<WordKey> = time.activeWords
}
