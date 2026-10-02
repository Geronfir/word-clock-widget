package com.geronfir.wordclock.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geronfir.wordclock.engine.EnglishVocabulary
import com.geronfir.wordclock.engine.EnglishWordGrid
import com.geronfir.wordclock.engine.PhraseFormatter
import com.geronfir.wordclock.engine.SemanticTime
import com.geronfir.wordclock.engine.TimeExpressionEngine
import com.geronfir.wordclock.widget.render.WordGridContent

/**
 * The Word Clock widget.
 *
 * Glance keeps the composable declarative; the heavy lifting (time -> words)
 * happens in the UI-free engine package. `provideGlance` only reads the current
 * time and hands the result to a renderer.
 */
class WordClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val semanticTime = TimeExpressionEngine().expressionNow()

        provideContent {
            GlanceTheme {
                WordClockContent(semanticTime)
            }
        }
    }

    @Composable
    private fun WordClockContent(semanticTime: SemanticTime) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF101014)))
                .padding(8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            WordGridContent(
                grid = EnglishWordGrid.grid,
                activeWords = semanticTime.activeWords,
                vocabulary = EnglishVocabulary,
                spokenText = PhraseFormatter(EnglishVocabulary).format(semanticTime),
            )
        }
    }
}
