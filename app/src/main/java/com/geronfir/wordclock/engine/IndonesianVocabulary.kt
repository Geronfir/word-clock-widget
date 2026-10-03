package com.geronfir.wordclock.engine

/**
 * Indonesian vocabulary.
 *
 * Indonesian does not build time phrases the way English does: the hour is named
 * first ("jam tiga"), minutes past use *lebih* and minutes to use *kurang*, and
 * the half hour reads as *setengah* **toward the next hour** ("jam setengah
 * delapan" = 07:30). That word order lives in [IndonesianTimeLocalization]; this
 * object only supplies the tokens, exactly like [EnglishVocabulary].
 */
object IndonesianVocabulary : WordVocabulary {
    override val languageTag: String = "id"

    private val words: Map<WordKey, String> = mapOf(
        WordKey.IT_IS to "JAM",
        WordKey.FIVE to "LIMA",
        WordKey.TEN to "SEPULUH",
        WordKey.QUARTER to "SEPEREMPAT",
        WordKey.TWENTY to "PULUH",
        WordKey.HALF to "SETENGAH",
        WordKey.PAST to "LEBIH",
        WordKey.TO to "KURANG",
        WordKey.OCLOCK to "PUKUL",
        WordKey.ONE to "SATU",
        WordKey.TWO to "DUA",
        WordKey.THREE to "TIGA",
        WordKey.FOUR to "EMPAT",
        WordKey.SIX to "ENAM",
        WordKey.SEVEN to "TUJUH",
        WordKey.EIGHT to "DELAPAN",
        WordKey.NINE to "SEMBILAN",
        WordKey.ELEVEN to "SEBELAS",
        WordKey.TWELVE to "DUABELAS",
        WordKey.AM to "PAGI",
        WordKey.PM to "MALAM",
        WordKey.IN_THE_MORNING to "PAGI",
        WordKey.IN_THE_AFTERNOON to "SIANG",
        WordKey.IN_THE_EVENING to "SORE",
        WordKey.AT_NIGHT to "MALAM",
    )

    override fun word(key: WordKey): String =
        words[key] ?: error("IndonesianVocabulary is missing a word for $key")
}
