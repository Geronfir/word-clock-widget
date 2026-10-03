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
 * empat" means 03:30, not 04:30. Because the word *order* differs from English,
 * both the readable phrase ([format]) and the grid words ([activeWords]) are
 * derived from one shared token sequence — otherwise the grid would light the
 * English order ("... LEBIH TIGA") and contradict the phrase.
 */
object IndonesianTimeLocalization : TimeLocalization {
    override val languageTag: String = "id"

    override fun format(time: SemanticTime): String =
        tokens(time).joinToString(" ") { IndonesianVocabulary.word(it) }

    /**
     * Indonesian has no word grid: its phrase order varies by case, so no single
     * static matrix can spell it out. The widget renders it as flowing text.
     */
    override fun gridWords(time: SemanticTime): List<WordKey>? = null

    /** The word sequence for [time], in Indonesian order, as language-neutral keys. */
    private fun tokens(time: SemanticTime): List<WordKey> {
        val hour = time.phraseHour12
        val minute = time.roundedMinute

        val out = mutableListOf(WordKey.IT_IS) // "JAM"
        when {
            minute == 0 -> {
                out += hourKey(hour)
            }
            minute == 30 -> {
                // "setengah <next hour>": 03:30 -> JAM SETENGAH EMPAT.
                out += WordKey.HALF
                out += hourKey(nextHour(hour))
            }
            minute < 30 -> {
                out += hourKey(hour)
                out += WordKey.PAST // "LEBIH"
                out += minuteKeys(minute)
            }
            else -> {
                // "kurang <minutes to the next hour> <next hour>".
                // The engine already points phraseHour12 at the next hour.
                out += hourKey(hour)
                out += WordKey.TO // "KURANG"
                out += minuteKeys(60 - minute)
            }
        }
        if (time.hasDayPeriod) out += dayPeriodKey(time.hour24)
        return out
    }

    private fun nextHour(hour12: Int): Int = hour12 % 12 + 1

    private fun hourKey(hour12: Int): WordKey = when (hour12) {
        1 -> WordKey.ONE; 2 -> WordKey.TWO; 3 -> WordKey.THREE; 4 -> WordKey.FOUR
        5 -> WordKey.FIVE; 6 -> WordKey.SIX; 7 -> WordKey.SEVEN; 8 -> WordKey.EIGHT
        9 -> WordKey.NINE; 10 -> WordKey.TEN; 11 -> WordKey.ELEVEN; else -> WordKey.TWELVE
    }

    /** Minutes as Indonesian words: 5/10/15/20/25 -> LIMA/…/DUA PULUH LIMA. */
    private fun minuteKeys(minutes: Int): List<WordKey> = when (minutes) {
        5 -> listOf(WordKey.FIVE)
        10 -> listOf(WordKey.TEN)
        15 -> listOf(WordKey.QUARTER)
        20 -> listOf(WordKey.TWO, WordKey.TWENTY)
        25 -> listOf(WordKey.TWO, WordKey.TWENTY, WordKey.FIVE)
        else -> emptyList()
    }

    private fun dayPeriodKey(hour24: Int): WordKey = when (hour24) {
        // Indonesian day parts differ from the English engine's buckets (which
        // treat 05:00–11:59 as morning): 00–10 pagi, 11–14 siang, 15–17 sore,
        // 18–23 malam.
        in 0..10 -> WordKey.IN_THE_MORNING    // PAGI
        in 11..14 -> WordKey.IN_THE_AFTERNOON // SIANG
        in 15..17 -> WordKey.IN_THE_EVENING   // SORE
        else -> WordKey.AT_NIGHT              // MALAM
    }
}
