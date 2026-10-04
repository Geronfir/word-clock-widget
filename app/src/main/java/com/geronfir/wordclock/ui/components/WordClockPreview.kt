package com.geronfir.wordclock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geronfir.wordclock.engine.LocalizationRegistry
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.engine.SemanticTime
import com.geronfir.wordclock.settings.WidgetSettings

/**
 * A live preview of the widget, rendered with the colours and font scale the
 * user picked in [settings].
 *
 * This deliberately mirrors what the Glance widget draws (grid vs flowing text,
 * active vs inactive words) without sharing code with it: the widget renders
 * through Glance and this renders through Compose, and the two have different
 * `Modifier`/`Color` types. Keeping them separate means a change here can never
 * break the home-screen widget.
 *
 * When the chosen language has no word grid (only English does — see
 * [LocalizationRegistry.gridOrNull]) the preview falls back to the spoken
 * phrase, exactly like the widget does.
 */
@Composable
fun WordClockPreview(
    settings: WidgetSettings,
    semanticTime: SemanticTime,
    modifier: Modifier = Modifier,
) {
    val activeColor = Color(settings.activeColorArgb.toInt())
    val inactiveColor = Color(settings.inactiveColorArgb.toInt())
    val backgroundColor = Color(settings.backgroundColorArgb.toInt())

    val localization = LocalizationRegistry.localization(settings.languageTag)
    val vocabulary = LocalizationRegistry.vocabulary(settings.languageTag)
    val grid = LocalizationRegistry.gridOrNull(settings.languageTag)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val gridWords = if (settings.representationStyle == RepresentationStyle.WORD_GRID) {
            localization.gridWords(semanticTime)
        } else {
            null
        }

        if (grid != null && gridWords != null) {
            val active = gridWords.toSet()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                grid.rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        row.forEach { key ->
                            val lit = key != null && key in active
                            Text(
                                text = key?.let(vocabulary::word) ?: "",
                                fontSize = (14f * settings.fontScale).sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (lit) activeColor else inactiveColor,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = localization.format(semanticTime),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                fontSize = (28f * settings.fontScale).sp,
                textAlign = TextAlign.Center,
                color = activeColor,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}
