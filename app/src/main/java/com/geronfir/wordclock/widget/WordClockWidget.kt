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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geronfir.wordclock.engine.LocalizationRegistry
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.engine.TimeExpressionEngine
import com.geronfir.wordclock.settings.WidgetSettings
import com.geronfir.wordclock.settings.WidgetSettingsStore
import com.geronfir.wordclock.widget.render.GridMetrics
import com.geronfir.wordclock.widget.render.WordGridContent

/**
 * The Word Clock widget.
 *
 * Glance keeps the composable declarative; the heavy lifting (time -> words)
 * happens in the UI-free engine package. `provideGlance` gathers the three inputs
 * a render needs — the semantic time, this instance's settings, and its size —
 * then delegates to a renderer.
 *
 * Each instance reads its own settings, so two widgets can differ in language,
 * format and colour without any global state.
 */
class WordClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = runCatching {
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        }.getOrDefault(-1)

        val settings = runCatching {
            WidgetSettingsStore(context).load(appWidgetId)
        }.getOrDefault(WidgetSettings.DEFAULT)

        val vocabulary = LocalizationRegistry.vocabulary(settings.languageTag)
        val grid = LocalizationRegistry.grid(settings.languageTag)
        val localization = LocalizationRegistry.localization(settings.languageTag)
        val semanticTime = TimeExpressionEngine(settings.toTimeConfig()).expressionNow()
        val metrics = WidgetSizeResolver.metricsFor(context, appWidgetId)
        val spoken = localization.format(semanticTime)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(settings.backgroundColorArgb)))
                        .padding(4.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                    when (settings.representationStyle) {
                        RepresentationStyle.WORD_GRID -> WordGridContent(
                            grid = grid,
                            activeWords = semanticTime.activeWords,
                            vocabulary = vocabulary,
                            metrics = metrics,
                            activeColor = Color(settings.activeColorArgb),
                            inactiveColor = Color(settings.inactiveColorArgb),
                            spokenText = spoken,
                        )

                        RepresentationStyle.FLOWING_TEXT -> FlowingTextContent(
                            text = spoken,
                            color = Color(settings.activeColorArgb),
                            metrics = metrics,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun FlowingTextContent(text: String, color: Color, metrics: GridMetrics) {
        Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = metrics.fontSize,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}