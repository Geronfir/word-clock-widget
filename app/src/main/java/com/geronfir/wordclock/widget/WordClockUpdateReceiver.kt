package com.geronfir.wordclock.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives the per-minute tick (and boot/locale/time-change broadcasts) and asks
 * Glance to re-render every placed widget instance.
 *
 * All the receiver does is trigger a re-render; the time -> words conversion and
 * layout work happens inside [WordClockWidget]. Keeping this class tiny is what
 * lets the widget stay cheap on battery.
 */
class WordClockUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in HANDLED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                WordClockWidget().updateAll(context)
                // Re-arm: ACTION_TIME_SET / locale changes can wipe repeating alarms.
                WidgetUpdateScheduler.schedule(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TICK = "com.geronfir.wordclock.action.TICK"

        val HANDLED_ACTIONS = setOf(
            ACTION_TICK,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
