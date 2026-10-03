package com.geronfir.wordclock.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
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
import com.geronfir.wordclock.engine.LocalizationRegistry
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.engine.TimeExpressionEngine
import com.geronfir.wordclock.settings.WidgetSettings
import com.geronfir.wordclock.settings.WidgetSettingsStore
import com.geronfir.wordclock.widget.render.GridMetrics
import com.geronfir.wordclock.widget.render.WordGridContent
import com.geronfir.wordclock.widget.render.flowingTextMetrics
import com.geronfir.wordclock.widget.render.gridMetricsFor

/**
 * The Word Clock widget.
 *
 * Glance keeps the composable declarative; the heavy lifting (time -> words)
 * happens in the UI-free engine package. Each instance reads its own settings, so
 * two widgets can differ in language, format and colour without any global state.
 *
 * Sizing uses [SizeMode.Exact] + [LocalSize] (see [GridMetrics] and
 * `docs/DECISIONS.md`): the widget host tells Glance the exact current size and
 * re-runs this composition on every resize, so the type scale follows the widget
 * immediately instead of waiting for the next minute tick.
 */
class WordClockWidget : GlanceAppWidget() {

    /**
     * Ask Glance for a distinct composition per size and hand us the *current*
     * size. The default ([SizeMode.Single]) renders once at the minimum size and
     * never updates on resize — which is why the text used to lag behind the
     * resize handle and could snap to the wrong scale.
     */
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = runCatching {
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        }.getOrDefault(-1)

        val settings = runCatching {
            WidgetSettingsStore(context).load(appWidgetId)
        }.getOrDefault(WidgetSettings.DEFAULT)

        val vocabulary = LocalizationRegistry.vocabulary(settings.languageTag)
        val grid = LocalizationRegistry.gridOrNull(settings.languageTag)
        val localization = LocalizationRegistry.localization(settings.languageTag)
        val semanticTime = TimeExpressionEngine(settings.toTimeConfig()).expressionNow()
        val spoken = localization.format(semanticTime)
        val gridWords = localization.gridWords(semanticTime)

        provideContent {
            // Read the size here, inside the composition: with SizeMode.Exact this
            // is the widget's real current size in dp (never pixels), and it is
            // recomputed on every resize. Convert to plain floats straight away so
            // the `DpSize` value class is never passed across a composable
            // boundary (that is what used to crash the Kotlin IR backend).
            val size = LocalSize.current
            val widthDp = size.width.value
            val heightDp = size.height.value

            val useGrid = settings.representationStyle == RepresentationStyle.WORD_GRID &&
                grid != null && gridWords != null

            val metrics = if (useGrid) {
                gridMetricsFor(widthDp, heightDp, settings.fontScale, grid!!.columns, grid.rows.size)
            } else {
                flowingTextMetrics(widthDp, heightDp, spoken.length, settings.fontScale)
            }

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(settings.backgroundColorArgb)))
                        .padding(4.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                    // A word grid is a physical layout that only some languages
                    // have. When the language has none (its phrase order cannot be
                    // laid out as a matrix), fall back to flowing text rather than
                    // light the wrong cells.
                    if (useGrid) {
                        WordGridContent(
                            grid = grid!!,
                            activeWords = gridWords!!,
                            vocabulary = vocabulary,
                            metrics = metrics,
                            activeColor = Color(settings.activeColorArgb),
                            inactiveColor = Color(settings.inactiveColorArgb),
                            spokenText = spoken,
                        )
                    } else {
                        FlowingTextContent(
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
