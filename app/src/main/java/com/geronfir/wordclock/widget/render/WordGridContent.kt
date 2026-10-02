package com.geronfir.wordclock.widget.render

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.geronfir.wordclock.engine.SemanticTime
import com.geronfir.wordclock.engine.WordGrid
import com.geronfir.wordclock.engine.WordKey
import com.geronfir.wordclock.engine.WordVocabulary

/**
 * Renders a [WordGrid] as the classic lit/unlit letter matrix.
 *
 * Active words use [activeColor], everything else [inactiveColor]. The renderer
 * consumes the grid + the engine's active words; it never computes time itself.
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

    Column(modifier = GlanceModifier.fillMaxWidth()) {
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
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        ),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
            }
        }
    }
}
