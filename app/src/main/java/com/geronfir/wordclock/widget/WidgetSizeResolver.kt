package com.geronfir.wordclock.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.compose.ui.unit.dp
import com.geronfir.wordclock.widget.render.GridMetrics
import com.geronfir.wordclock.widget.render.gridMetricsFor

/**
 * Reads the size the user gave this widget instance and converts it to metrics.
 *
 * Uses [AppWidgetManager.getAppWidgetOptions] rather than Glance's `LocalSize`
 * because the latter triggers a Kotlin IR backend crash in this toolchain (see
 * `GridMetrics`). Options are only available once the widget has been laid out, so
 * the provider-info minimum is used as a fallback for the very first render.
 */
object WidgetSizeResolver {

    /** Default when the host has not reported options yet (matches the XML minimum). */
    private const val FALLBACK_DP = 180f

    fun metricsFor(context: Context, appWidgetId: Int): GridMetrics {
        val options = runCatching {
            AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        }.getOrNull()

        val (widthDp, heightDp) = sizeInDp(context, options)
        return gridMetricsFor(widthDp, heightDp)
    }

    private fun sizeInDp(context: Context, options: Bundle?): Pair<Float, Float> {
        if (options == null) return FALLBACK_DP to FALLBACK_DP

        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        if (minWidth <= 0 || minHeight <= 0) return FALLBACK_DP to FALLBACK_DP

        val density = context.resources.displayMetrics.density
        val widthDp = if (density > 0f) minWidth / density else minWidth.toFloat()
        val heightDp = if (density > 0f) minHeight / density else minHeight.toFloat()
        return widthDp to heightDp
    }
}
