package com.geronfir.wordclock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.Toast
import com.geronfir.wordclock.R
import com.geronfir.wordclock.engine.TimeExpressionEngine
import com.geronfir.wordclock.settings.ThemePreset
import com.geronfir.wordclock.settings.WidgetSettings
import com.geronfir.wordclock.settings.WidgetSettingsStore
import com.geronfir.wordclock.ui.components.WordClockPreview
import com.geronfir.wordclock.ui.components.fontLabel
import com.geronfir.wordclock.ui.components.installedWidgetIds
import com.geronfir.wordclock.ui.components.languageLabel
import com.geronfir.wordclock.ui.components.styleLabel
import com.geronfir.wordclock.ui.theme.WordClockTheme
import com.geronfir.wordclock.widget.WordClockWidgetReceiver
import kotlinx.coroutines.delay

/** Word clocks change at most once a minute, so wake up on the minute boundary. */
private const val MILLIS_PER_MINUTE = 60_000L

/** Milliseconds until the next whole minute, so the preview flips exactly on time. */
private fun millisUntilNextMinute(): Long =
    MILLIS_PER_MINUTE - (System.currentTimeMillis() % MILLIS_PER_MINUTE)

/**
 * Home: a live preview of the widget plus the "add the widget" call to action.
 *
 * The preview reuses the real engine ([TimeExpressionEngine]) and the real
 * English word grid, so what the user sees here is exactly what the widget
 * will light up. It re-renders on a delayed loop that wakes on the minute
 * boundary ([millisUntilNextMinute]); the engine snaps to the nearest five
 * minutes, so the lit words change at most once per minute.
 */
@Composable
fun HomeScreen() {
    // Epoch-seconds snapshot. One Long is cheap to recompose and avoids
    // holding a java.time value class across composition.
    //
    // Seeded with the current time, not 0: the LaunchedEffect below only runs
    // after the first frame, so starting at 0 would light up the words for Unix
    // epoch (e.g. 07:00 in Asia/Jakarta) and flash a wrong time on open.
    var nowEpochSecond by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowEpochSecond = System.currentTimeMillis() / 1000L
            // Sleep until the minute rolls over, then recompute once.
            delay(millisUntilNextMinute())
        }
    }

    // The preview must show the widget's OWN colours, not the app theme's, or it
    // lies about what the user will see on the home screen. So load the settings
    // of the first placed widget (falling back to the defaults when none exists).
    val context = LocalContext.current
    var settings by remember { mutableStateOf(WidgetSettings.DEFAULT) }
    var hasWidget by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val ids = installedWidgetIds(context)
        hasWidget = ids.isNotEmpty()
        if (hasWidget) {
            settings = runCatching { WidgetSettingsStore(context).load(ids.first()) }
                .getOrDefault(WidgetSettings.DEFAULT)
        }
    }

    // The engine follows the loaded settings, so the 24-hour and day-period
    // choices are reflected here exactly as they are on the home screen.
    val timeConfig = settings.toTimeConfig()
    val engine = remember(timeConfig) { TimeExpressionEngine(timeConfig) }
    val semanticTime = remember(nowEpochSecond, engine) {
        val now = java.time.Instant.ofEpochSecond(nowEpochSecond)
            .atZone(java.time.ZoneId.systemDefault())
        engine.expressionAt(now.hour, now.minute)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.main_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        WordClockPreview(settings = settings, semanticTime = semanticTime)
        Spacer(Modifier.height(24.dp))
        AddWidgetButton()
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.main_add_hint),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        SettingsSummary(settings = settings, hasWidget = hasWidget)
    }
}

/**
 * A short, read-only recap of what the placed widget currently shows.
 *
 * It mirrors the Display tab, but as plain text: the point is to answer "what is
 * my widget doing right now?" without making the user tap through. When nothing
 * is placed it says so instead of showing meaningless defaults.
 */
@Composable
private fun SettingsSummary(settings: WidgetSettings, hasWidget: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.main_settings_summary_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        if (!hasWidget) {
            Text(
                text = stringResource(R.string.home_summary_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        SummaryRow(stringResource(R.string.display_language), languageLabel(settings.languageTag))
        SummaryRow(stringResource(R.string.display_style), styleLabel(settings.representationStyle))
        SummaryRow(
            stringResource(R.string.display_format),
            buildString {
                append(
                    stringResource(
                        if (settings.use24Hour) R.string.home_summary_format_24
                        else R.string.home_summary_format_12,
                    ),
                )
                append(", ")
                append(
                    stringResource(
                        if (settings.includeDayPeriod) R.string.home_summary_day_period_on
                        else R.string.home_summary_day_period_off,
                    ),
                )
            },
        )
        SummaryRow(stringResource(R.string.display_font_size), fontLabel(settings.fontScale))
        SummaryRow(stringResource(R.string.display_colors), stringResource(ThemePreset.matching(settings).labelRes))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Asks the launcher to pin this widget to the home screen.
 *
 * Uses `requestPinAppWidget` (API 26+, which is this app's `minSdk`) so the
 * system shows its own confirmation dialog — no custom UI, no guessing about
 * the launcher. When the launcher does not support pinning (some OEM launchers
 * refuse), a toast repeats the manual long-press instructions instead of
 * leaving the button looking broken.
 */
@Composable
private fun AddWidgetButton() {
    val context = LocalContext.current
    // Resolve the fallback text through stringResource so a Configuration change
    // (locale, font scale) re-resolves it — lint's LocalContextGetResourceValueCall.
    val manualHint = stringResource(R.string.main_add_hint)
    Button(
        onClick = {
            val manager = AppWidgetManager.getInstance(context)
            if (manager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, WordClockWidgetReceiver::class.java)
                manager.requestPinAppWidget(provider, null, null)
            } else {
                Toast.makeText(context, manualHint, Toast.LENGTH_LONG).show()
            }
        },
    ) {
        Text(stringResource(R.string.main_add_widget_button))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    WordClockTheme {
        Surface { HomeScreen() }
    }
}
