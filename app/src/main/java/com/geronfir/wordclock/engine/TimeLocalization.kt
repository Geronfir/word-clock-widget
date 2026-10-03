package com.geronfir.wordclock.engine

/**
 * Turns a [SemanticTime] into a human-readable phrase *in one language*.
 *
 * Word order and idiom live here, per language — never in the UI. The widget
 * asks the registry for the localization that matches the user's language and
 * calls [format]; the [TimeExpressionEngine] itself stays language-neutral.
 *
 * English and Indonesian are different enough that a shared "join the active
 * words" formatter cannot serve both: Indonesian says "jam" first and reads the
 * half-hour as *half toward the next hour*. Giving each language its own
 * formatter is what the extensibility requirement actually demands.
 */
interface TimeLocalization {
    val languageTag: String
    fun format(time: SemanticTime): String
}

/** English: "IT IS <minutes> PAST/TO <hour>", or "IT IS <hour> O'CLOCK". */
object EnglishTimeLocalization : TimeLocalization {
    override val languageTag: String = "en"

    override fun format(time: SemanticTime): String =
        PhraseFormatter(EnglishVocabulary).format(time)
}
