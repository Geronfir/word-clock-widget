package com.geronfir.wordclock.engine

/**
 * A language's vocabulary: which word each [WordKey] maps to.
 *
 * Implementations are plain data, so adding a language never touches the engine
 * or the widget. A language whose grammar cannot be expressed by these keys
 * implements its own [TimeLocalization] instead of forcing English structure.
 */
interface WordVocabulary {
    val languageTag: String
    fun word(key: WordKey): String
}

/** English vocabulary. */
object EnglishVocabulary : WordVocabulary {
    override val languageTag: String = "en"

    private val words: Map<WordKey, String> = mapOf(
        WordKey.IT_IS to "IT IS",
        WordKey.FIVE to "FIVE",
        WordKey.TEN to "TEN",
        WordKey.QUARTER to "QUARTER",
        WordKey.TWENTY to "TWENTY",
        WordKey.HALF to "HALF",
        WordKey.PAST to "PAST",
        WordKey.TO to "TO",
        WordKey.OCLOCK to "O'CLOCK",
        WordKey.ONE to "ONE",
        WordKey.TWO to "TWO",
        WordKey.THREE to "THREE",
        WordKey.FOUR to "FOUR",
        WordKey.SIX to "SIX",
        WordKey.SEVEN to "SEVEN",
        WordKey.EIGHT to "EIGHT",
        WordKey.NINE to "NINE",
        WordKey.ELEVEN to "ELEVEN",
        WordKey.TWELVE to "TWELVE",
        WordKey.AM to "AM",
        WordKey.PM to "PM",
        WordKey.IN_THE_MORNING to "IN THE MORNING",
        WordKey.IN_THE_AFTERNOON to "IN THE AFTERNOON",
        WordKey.IN_THE_EVENING to "IN THE EVENING",
        WordKey.AT_NIGHT to "AT NIGHT",
    )

    override fun word(key: WordKey): String =
        words[key] ?: error("EnglishVocabulary is missing a word for $key")
}
