package com.geronfir.wordclock.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.geronfir.wordclock.widget.WordClockWidgetReceiver

/**
 * The ids of the Word Clock widgets currently placed on the home screen, in the
 * launcher's order (empty when none is placed).
 *
 * Wrapped in `runCatching` because `AppWidgetManager` can throw on some OEM
 * builds when the provider is not registered yet; a screen that shows "no widget
 * yet" is always better than a crash.
 */
fun installedWidgetIds(context: Context): IntArray = runCatching {
    AppWidgetManager.getInstance(context).getAppWidgetIds(
        ComponentName(context, WordClockWidgetReceiver::class.java),
    )
}.getOrDefault(IntArray(0))
