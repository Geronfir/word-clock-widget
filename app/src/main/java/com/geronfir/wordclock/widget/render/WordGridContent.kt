package com.geronfir.wordclock.widget.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
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
 * Accessibility: the lit words are exposed as one content description, so screen
 * readers announce "IT IS QUARTER PAST THREE" instead of reading 25 loose cells.
 * Active cells also use a bold weight, so state is not conveyed by colour alone.
 *
 * [metrics] is passed in rather than read from `LocalSize` — see [GridMetrics].
 */
@Composable
fun WordGridContent(
    grid: WordGrid,
    activeWords: Collection<WordKey>,
    vocabulary: WordVocabulary,
    metrics: GridMetrics,
    activeColor: Color = Color(0xFFE8E8EC),
    inactiveColor: Color = Color(0xFF3A3A42),
    spokenText: String? = null,
) {
    val active = activeWords.toSet()

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(metrics.padding)
            .then(
                if (spokenText.isNullOrBlank()) {
                    GlanceModifier
                } else {
                    GlanceModifier.semantics { contentDescription = spokenText }
                },
            ),
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
                            fontSize = metrics.fontSize,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        ),
                        modifier = GlanceModifier.width(metrics.cellWidth),
                    )
                }
            }
        }
    }
}
