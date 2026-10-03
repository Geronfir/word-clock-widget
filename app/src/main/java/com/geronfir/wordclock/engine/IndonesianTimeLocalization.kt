package com.geronfir.wordclock.engine

/**
 * Indonesian time phrasing.
 *
 * Indonesian names the hour first and then qualifies it, which is the opposite of
 * English's "minutes past the hour":
 *
 * ```
 * 03:00  -> JAM TIGA
 * 03:10  -> JAM TIGA LEBIH SEPULUH        (three plus ten)
 * 03:15  -> JAM TIGA LEBIH SEPEREMPAT
 * 03:30  -> JAM SETENGAH EMPAT            (half *toward* four — a classic trap)
 * 03:40  -> JAM EMPAT KURANG DUA PULUH    (four minus twenty)
 * 03:45  -> JAM EMPAT KURANG SEPEREMPAT
 * ```
 *
 * The half-hour rule is the one most implementations get wrong: "setengah
 * empat" means 03:30, not 04:30. It is expressed here, per language, rather than
 * being forced into the engine's English-shaped word list.
 */
object IndonesianTimeLocalization : TimeLocalization {
    override val languageTag: String = "id"

    override fun format(time: SemanticTime): String {
        val hour = time.phraseHour12
        val minute = time.roundedMinute

        val parts = buildList {
            add(IndonesianVocabulary.word(WordKey.IT_IS)) // "JAM"
            when {
                minute == 0 -> {
                    add(hourWord(hour))
                }
                minute == 30 -> {
                    // "setengah <next hour>": 03:30 -> JAM SETENGAH EMPAT.
                    add(IndonesianVocabulary.word(WordKey.HALF))
                    add(hourWord(nextHour(hour)))
                }
                minute < 30 -> {
                    add(hourWord(hour))
                    add(IndonesianVocabulary.word(WordKey.PAST)) // "LEBIH"
                    addAll(minuteWords(minute))
                }
                else -> {
                    // "kurang <minutes to the next hour> <next hour>".
                    // The engine already points phraseHour12 at the next hour.
                    add(hourWord(hour))
                    add(IndonesianVocabulary.word(WordKey.TO)) // "KURANG"
                    addAll(minuteWords(60 - minute))
                }
            }
            if (time.hasDayPeriod) add(dayPeriodWord(time.hour24))
        }

        return parts.joinToString(" ")
    }

    private fun nextHour(hour12: Int): Int = hour12 % 12 + 1

    private fun hourWord(hour12: Int): String = IndonesianVocabulary.word(
        when (hour12) {
            1 -> WordKey.ONE; 2 -> WordKey.TWO; 3 -> WordKey.THREE; 4 -> WordKey.FOUR
            5 -> WordKey.FIVE; 6 -> WordKey.SIX; 7 -> WordKey.SEVEN; 8 -> WordKey.EIGHT
            9 -> WordKey.NINE; 10 -> WordKey.TEN; 11 -> WordKey.ELEVEN; else -> WordKey.TWELVE
        },
    )

    /** Minutes as Indonesian words: 5/10/15/20/25 -> LIMA/…/DUA PULUH LIMA. */
    private fun minuteWords(minutes: Int): List<String> = when (minutes) {
        5 -> listOf(IndonesianVocabulary.word(WordKey.FIVE))
        10 -> listOf(IndonesianVocabulary.word(WordKey.TEN))
        15 -> listOf(IndonesianVocabulary.word(WordKey.QUARTER))
        20 -> listOf(
            IndonesianVocabulary.word(WordKey.TWO),
            IndonesianVocabulary.word(WordKey.TWENTY),
        )
        25 -> listOf(
            IndonesianVocabulary.word(WordKey.TWO),
            IndonesianVocabulary.word(WordKey.TWENTY),
            IndonesianVocabulary.word(WordKey.FIVE),
        )
        else -> emptyList()
    }

    private fun dayPeriodWord(hour24: Int): String = IndonesianVocabulary.word(
        // Indonesian day parts differ from the English engine's buckets (which
        // treat 05:00–11:59 as morning). Indonesian: 00–10 pagi, 11–14 siang,
        // 15–17 sore, 18–23 malam.
        when (hour24) {
            in 0..10 -> WordKey.IN_THE_MORNING    // PAGI
            in 11..14 -> WordKey.IN_THE_AFTERNOON // SIANG
            in 15..17 -> WordKey.IN_THE_EVENING   // SORE
            else -> WordKey.AT_NIGHT              // MALAM
        },
    )
}
