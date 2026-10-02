package com.geronfir.wordclock.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Manifest entry point for the widget.
 *
 * Glance's receiver already handles APPWIDGET_UPDATE / ENABLED / DISABLED /
 * DELETED / OPTIONS_CHANGED. We override the hooks only to keep our own
 * per-minute alarm in sync with the widget lifecycle.
 */
class WordClockWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WordClockWidget()

    override fun onEnabled(context: android.content.Context) {
        super.onEnabled(context)
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: android.content.Context) {
        super.onDisabled(context)
        WidgetUpdateScheduler.cancel(context)
    }

    override fun onDeleted(context: android.content.Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // If the user removed the last instance, stop the alarm.
        if (appWidgetIds.isEmpty()) {
            WidgetUpdateScheduler.cancel(context)
        }
    }
}
