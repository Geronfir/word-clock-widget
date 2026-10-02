package com.geronfir.wordclock.widget.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.geronfir.wordclock.engine.WordGrid
import com.geronfir.wordclock.engine.WordKey
import com.geronfir.wordclock.engine.WordVocabulary

/**
 * Renders a [WordGrid] as the classic lit/unlit word matrix.
 *
 * Active words use [activeColor], everything else [inactiveColor]. The renderer
 * consumes the grid plus the engine's active words; it never computes time itself.
 *
 * Layout note: each cell is a fixed-width box (`GlanceModifier.width`) rather than
 * a weighted cell, because Glance's `defaultWeight()` is not available in the
 * pinned Glance version. Fixed boxes keep the columns aligned at every widget size.
 */
@Composable
fun WordGridContent(
    grid: WordGrid,
    activeWords: Collection<WordKey>,
    vocabulary: WordVocabulary,
    activeColor: Color = Color(0xFFE8E8EC),
    inactiveColor: Color = Color(0xFF3A3A42),
) {
    val active = activeWords.toSet()

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(4.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        grid.rows.forEach { row ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                row.forEach { key ->
                    val label = if (key == null) "" else vocabulary.word(key)
                    val isActive = key != null && key in active
                    Text(
                        text = label.ifEmpty { " " },
                        style = TextStyle(
                            color = ColorProvider(if (isActive) activeColor else inactiveColor),
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        ),
                        modifier = GlanceModifier.width(56.dp),
                    )
                }
            }
        }
    }
}
