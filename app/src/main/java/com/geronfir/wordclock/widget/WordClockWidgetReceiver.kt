package com.geronfir.wordclock.widget

import android.content.Context
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.geronfir.wordclock.settings.WidgetSettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manifest entry point for the widget.
 *
 * Glance's receiver already handles APPWIDGET_UPDATE / ENABLED / DISABLED /
 * DELETED / OPTIONS_CHANGED. We override the hooks to keep the per-minute alarm
 * and the per-instance settings in step with the widget lifecycle.
 *
 * Resizing is deliberately NOT handled here: [WordClockWidget] uses
 * `SizeMode.Exact`, so the host re-runs the composition itself on every resize.
 * Manually calling `updateAll` from `onAppWidgetOptionsChanged` only duplicated
 * that work and added latency.
 */
class WordClockWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WordClockWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateScheduler.cancel(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Drop this instance's saved configuration so a future widget that reuses
        // the same appWidgetId starts clean instead of inheriting stale settings.
        val store = WidgetSettingsStore(context)
        CoroutineScope(Dispatchers.Default).launch {
            appWidgetIds.forEach { id -> runCatching { store.delete(id) } }
        }
        if (appWidgetIds.isEmpty()) WidgetUpdateScheduler.cancel(context)
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        // Widgets restored from backup get new ids; copy the old configuration over
        // so the user's choices survive a device restore.
        val store = WidgetSettingsStore(context)
        CoroutineScope(Dispatchers.Default).launch {
            oldWidgetIds.zip(newWidgetIds).forEach { (oldId, newId) ->
                runCatching {
                    val settings = store.load(oldId)
                    store.save(newId, settings)
                    store.delete(oldId)
                }
            }
        }
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        // SizeMode.Exact already re-composes with the new size; nothing extra here.
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}
