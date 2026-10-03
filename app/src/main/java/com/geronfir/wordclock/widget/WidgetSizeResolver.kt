package com.geronfir.wordclock.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
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

    fun metricsFor(context: Context, appWidgetId: Int): GridMetrics {
        val options = runCatching {
            AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        }.getOrNull()

        val (widthDp, heightDp) = sizeInDp(options)
        return gridMetricsFor(widthDp, heightDp)
    }

    private fun sizeInDp(options: Bundle?): Pair<Float, Float> {
        if (options == null) return FALLBACK_WIDGET_DP to FALLBACK_WIDGET_DP
        return widgetSizeInDp(
            minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
            minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
        )
    }
}

/** Default when the host has not reported options yet (matches the XML minimum). */
internal const val FALLBACK_WIDGET_DP = 180f

/**
 * Converts the raw `AppWidgetManager` option ints to dp floats.
 *
 * `OPTION_APPWIDGET_MIN_WIDTH` / `OPTION_APPWIDGET_MIN_HEIGHT` are documented as
 * **"in dips"**
 * (https://developer.android.com/reference/android/appwidget/AppWidgetManager),
 * so they are already dp and are passed through unchanged. The previous code
 * divided them by `displayMetrics.density`, shrinking every widget by that factor
 * (e.g. a 180 dp widget became ~65 dp on a 2.75x device) and pinning it to the
 * smallest font bucket. The XML minimum ([FALLBACK_WIDGET_DP]) is also in dp, so
 * this keeps both paths consistent.
 *
 * Pure function so it can be unit-tested without an Android runtime.
 */
internal fun widgetSizeInDp(minWidthDp: Int, minHeightDp: Int): Pair<Float, Float> {
    if (minWidthDp <= 0 || minHeightDp <= 0) return FALLBACK_WIDGET_DP to FALLBACK_WIDGET_DP
    return minWidthDp.toFloat() to minHeightDp.toFloat()
}
