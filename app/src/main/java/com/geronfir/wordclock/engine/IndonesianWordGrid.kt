package com.geronfir.wordclock.engine

/**
 * The Indonesian grid: 5x5, holding every Indonesian *core* token exactly once.
 *
 * Like the English grid, shared tokens (here PULUH, used for "dua puluh") appear a
 * single time, and the day-period phrases (pagi/siang/sore/malam) are rendered by
 * the flowing-text style rather than squeezed into a cell — only the single-word
 * AM/PM equivalents (PAGI/MALAM) get a cell, mirroring the English grid.
 *
 * The grid is a property of the language, so Indonesian lays its words out
 * differently from English without touching the widget or the engine.
 */
object IndonesianWordGrid {
    val grid: WordGrid = WordGrid(
        languageTag = "id",
        rows = listOf(
            listOf(WordKey.IT_IS, WordKey.OCLOCK, WordKey.FIVE, WordKey.TEN, WordKey.HALF),
            listOf(WordKey.PAST, WordKey.TO, WordKey.QUARTER, WordKey.TWENTY, WordKey.TWO),
            listOf(WordKey.THREE, WordKey.FOUR, WordKey.SIX, WordKey.SEVEN, WordKey.EIGHT),
            listOf(WordKey.NINE, WordKey.ELEVEN, WordKey.TWELVE, WordKey.ONE, WordKey.AM),
            listOf(null, null, WordKey.PM, null, null),
        ),
    )
}
