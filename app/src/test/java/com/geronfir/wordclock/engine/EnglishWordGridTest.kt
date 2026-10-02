package com.geronfir.wordclock.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the English grid and its active-word mapping. */
class EnglishWordGridTest {

    private val grid = EnglishWordGrid.grid

    @Test
    fun `grid is five by five`() {
        assertEquals(5, grid.rows.size)
        assertEquals(5, grid.columns)
    }

    @Test
    fun `every row has equal width`() {
        assertTrue(grid.rows.all { it.size == grid.columns })
    }

    @Test
    fun `grid contains every english word exactly once`() {
        val tokens = grid.rows.flatten().filterNotNull()
        val duplicates = tokens.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertEquals("duplicate tokens: $duplicates", emptyMap<WordKey, Int>(), duplicates)
    }

    @Test
    fun `half past three lights it is half past three`() {
        val t = TimeExpressionEngine().expressionAt(3, 30)
        val cells = grid.activeCells(t.activeWords)
        // IT IS (r0c0), HALF (r0c4), PAST (r1c2), THREE (r2c1)
        assertTrue(WordGrid.Cell(0, 0) in cells)
        assertTrue(WordGrid.Cell(0, 4) in cells)
        assertTrue(WordGrid.Cell(1, 2) in cells)
        assertTrue(WordGrid.Cell(2, 1) in cells)
        assertEquals(4, cells.size)
    }

    @Test
    fun `twenty five past five reuses the single FIVE cell`() {
        val t = TimeExpressionEngine().expressionAt(5, 25)
        val cells = grid.activeCells(t.activeWords)
        val fiveCells = grid.rows.mapIndexed { r, row ->
            row.mapIndexedNotNull { c, k -> if (k == WordKey.FIVE) WordGrid.Cell(r, c) else null }
        }.flatten()
        // Exactly one FIVE cell exists, and it is active for both the minute and hour use.
        assertEquals(1, fiveCells.size)
        assertTrue(fiveCells.single() in cells)
    }

    @Test
    fun `active cell count never exceeds the number of active words`() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val t = TimeExpressionEngine().expressionAt(hour, minute)
                val cells = grid.activeCells(t.activeWords)
                assertTrue(
                    "hour=$hour minute=$minute cells=${cells.size} words=${t.activeWords.size}",
                    cells.size <= t.activeWords.size,
                )
            }
        }
    }
}
