package com.geronfir.wordclock.engine

/**
 * A localized "grid": the fixed matrix of tokens a word clock physically shows.
 *
 * The grid is a property of the *language/layout*, not of the widget: different
 * languages may need different dimensions or extra filler words. `null` cells are
 * intentionally blank.
 *
 * Tokens are [WordKey]s so the renderer can style active vs inactive without
 * re-deriving the phrase, and so a token shared by several keys (the shared
 * FIVE/TEN rows of a physical clock) resolves to the same cell.
 */
data class WordGrid(
    val languageTag: String,
    /** Row-major tokens; every row must have the same length. */
    val rows: List<List<WordKey?>>,
) {
    val columns: Int get() = rows.firstOrNull()?.size ?: 0
    val cellCount: Int get() = rows.size * columns

    init {
        require(rows.isNotEmpty()) { "grid must have at least one row" }
        val width = rows.first().size
        require(width > 0) { "grid rows must not be empty" }
        require(rows.all { it.size == width }) { "all grid rows must have the same length" }
    }

    /** Every non-blank cell whose key is part of [activeWords], as (row, column) pairs. */
    fun activeCells(activeWords: Collection<WordKey>): Set<Cell> {
        val active = activeWords.toSet()
        val cells = mutableSetOf<Cell>()
        rows.forEachIndexed { r, row ->
            row.forEachIndexed { c, key ->
                if (key != null && key in active) cells += Cell(r, c)
            }
        }
        return cells
    }

    data class Cell(val row: Int, val column: Int)
}

/**
 * The English grid: 5x5, holding all 21 English tokens exactly once.
 *
 * FIVE and TEN deliberately appear a single time each and serve as both minute
 * units and hour words, mirroring how a physical English clock shares letters.
 * Day-period words that are whole phrases ("in the morning") are rendered by the
 * flowing-text style rather than squeezed into a cell — see `docs/DECISIONS.md`.
 */
object EnglishWordGrid {
    val grid: WordGrid = WordGrid(
        languageTag = "en",
        rows = listOf(
            listOf(WordKey.IT_IS, WordKey.QUARTER, WordKey.TWENTY, WordKey.FIVE, WordKey.HALF),
            listOf(WordKey.TEN, WordKey.TO, WordKey.PAST, WordKey.NINE, WordKey.ONE),
            listOf(WordKey.SIX, WordKey.THREE, WordKey.TWELVE, WordKey.SEVEN, WordKey.EIGHT),
            listOf(WordKey.FOUR, WordKey.TWO, WordKey.ELEVEN, WordKey.OCLOCK, WordKey.AM),
            listOf(null, null, WordKey.PM, null, null),
        ),
    )
}
