package com.geronfir.wordclock.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import com.geronfir.wordclock.widget.render.GridMetrics
import com.geronfir.wordclock.widget.render.WordGridContent

/**
 * The Word Clock widget.
 *
 * Glance keeps the composable declarative; the heavy lifting (time -> words)
 * happens in the UI-free engine package. `provideGlance` reads the current time
 * and the instance's size, then hands both to a renderer.
 */
class WordClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val semanticTime = TimeExpressionEngine().expressionNow()
        val metrics = metricsFor(context, id)

        provideContent {
            GlanceTheme {
                WordClockContent(semanticTime, metrics)
            }
        }
    }

    /**
     * Resolves the size *outside* the composition. Reading Glance's `LocalSize`
     * inside a composable crashes the Kotlin IR backend in this toolchain.
     */
    private fun metricsFor(context: Context, id: GlanceId): GridMetrics {
        val appWidgetId = runCatching {
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        }.getOrDefault(-1)
        return WidgetSizeResolver.metricsFor(context, appWidgetId)
    }

    @Composable
    private fun WordClockContent(semanticTime: SemanticTime, metrics: GridMetrics) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF101014)))
                .padding(4.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            WordGridContent(
                grid = EnglishWordGrid.grid,
                activeWords = semanticTime.activeWords,
                vocabulary = EnglishVocabulary,
                metrics = metrics,
                spokenText = PhraseFormatter(EnglishVocabulary).format(semanticTime),
            )
        }
    }
}
