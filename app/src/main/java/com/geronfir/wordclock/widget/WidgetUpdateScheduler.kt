package com.geronfir.wordclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

/**
 * Drives per-minute widget refreshes without a background service.
 *
 * Android only allows `updatePeriodMillis` down to 30 minutes, which is far too
 * coarse for a clock. The platform-supported alternative is an inexact
 * [AlarmManager] alarm that wakes our own broadcast receiver once a minute; the
 * receiver then asks Glance to re-render the widget and re-arms the alarm.
 *
 * We use `setInexactRepeating` deliberately: a clock face does not need
 * millisecond precision, and inexact alarms are far friendlier to the battery
 * and are not subject to exact-alarm permission rules.
 */
object WidgetUpdateScheduler {

    private const val INTERVAL_MS = 60_000L

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context)

        // Align the first tick to the next minute boundary so the displayed time
        // changes as close as possible to when the minute actually flips.
        val millisUntilNextMinute = INTERVAL_MS - (System.currentTimeMillis() % INTERVAL_MS)

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + millisUntilNextMinute,
            INTERVAL_MS,
            pendingIntent,
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WordClockUpdateReceiver::class.java).apply {
            action = WordClockUpdateReceiver.ACTION_TICK
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}
