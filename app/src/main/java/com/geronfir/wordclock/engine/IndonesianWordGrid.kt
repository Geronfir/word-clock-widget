package com.geronfir.wordclock.engine

/**
 * The Indonesian grid: 5x5, holding the Indonesian core tokens.
 *
 * Two deliberate departures from the English grid:
 *  - **DUA appears twice.** Indonesian needs DUA both as an hour ("jam dua") and
 *    as the tens word ("dua puluh", "dua belas"). English gets away with a single
 *    shared cell because its grid words never collide; Indonesian's do, so the
 *    grid carries two DUA cells.
 *  - **No PUKUL cell.** "PUKUL" is the Indonesian word for "o'clock", but the
 *    Indonesian phrase for :00 is just "jam <hour>", so the token is never lit.
 *    Leaving it out keeps every cell meaningful.
 *
 * The day-period phrases (pagi/siang/sore/malam) are rendered by the flowing-text
 * style; the single-word AM/PM equivalents (PAGI/MALAM) get a cell, mirroring the
 * English grid.
 */
object IndonesianWordGrid {
    val grid: WordGrid = WordGrid(
        languageTag = "id",
        rows = listOf(
            listOf(WordKey.IT_IS, WordKey.QUARTER, WordKey.TWENTY, WordKey.FIVE, WordKey.HALF),
            listOf(WordKey.TEN, WordKey.TO, WordKey.PAST, WordKey.NINE, WordKey.ONE),
            listOf(WordKey.SIX, WordKey.THREE, WordKey.TWELVE, WordKey.SEVEN, WordKey.EIGHT),
            listOf(WordKey.FOUR, WordKey.TWO, WordKey.ELEVEN, WordKey.AM, WordKey.PM),
            listOf(WordKey.TWO, null, null, null, null),
        ),
    )
}
