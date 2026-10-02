package com.geronfir.wordclock.engine

/**
 * Turns a [SemanticTime] into a readable phrase using a [WordVocabulary].
 *
 * This is the "flowing text" representation. It shares the exact same
 * [TimeExpressionEngine] output as the word grid, so the two layouts can never
 * drift apart. Word order lives here, per language — not in the UI.
 */
class PhraseFormatter(private val vocabulary: WordVocabulary) {

    fun format(time: SemanticTime): String {
        val parts = time.activeWords.map { vocabulary.word(it) }
        return parts.joinToString(" ")
    }
}
