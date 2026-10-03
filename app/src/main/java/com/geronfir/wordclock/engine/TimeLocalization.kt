package com.geronfir.wordclock.engine

/**
 * Turns a [SemanticTime] into the words of *one language*.
 *
 * A language owns two things the widget needs:
 *  - [format]: the readable phrase (flowing-text style), and
 *  - [activeWords]: which [WordKey]s to light in a word grid.
 *
 * Both must come from the same place. English reads minutes *past* the hour, so
 * the engine's own word list happens to fit; Indonesian reads the hour first and
 * the half hour as *toward the next hour*, so its grid words are genuinely
 * different from the English ones ("JAM SETENGAH EMPAT", not "... LEBIH TIGA").
 * Keeping [activeWords] here is what stops the grid and the phrase from drifting
 * apart in a non-English language.
 */
interface TimeLocalization {
    val languageTag: String

    /** The readable phrase, in this language's own word order. */
    fun format(time: SemanticTime): String

    /** The words to light in a word grid, in this language's own order. */
    fun activeWords(time: SemanticTime): List<WordKey>
}

/**
 * English: the engine's word list is already English-shaped, so the grid words
 * are exactly what the engine produced ("IT IS <minutes> PAST/TO <hour>").
 */
object EnglishTimeLocalization : TimeLocalization {
    override val languageTag: String = "en"

    override fun format(time: SemanticTime): String =
        PhraseFormatter(EnglishVocabulary).format(time)

    override fun activeWords(time: SemanticTime): List<WordKey> = time.activeWords
}
