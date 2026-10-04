package com.geronfir.wordclock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.Toast
import com.geronfir.wordclock.R
import com.geronfir.wordclock.engine.LocalizationRegistry
import com.geronfir.wordclock.engine.SemanticTime
import com.geronfir.wordclock.engine.TimeExpressionEngine
import com.geronfir.wordclock.engine.TimeLocalization
import com.geronfir.wordclock.engine.WordGrid
import com.geronfir.wordclock.engine.WordVocabulary
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
 * will light up. It re-renders on a delayed loop ([TICK_MS]); the engine
 * snaps to the nearest five minutes, so the lit words change once per 5.
 */
@Composable
fun HomeScreen() {
    // Epoch-seconds snapshot. One Long is cheap to recompose and avoids
    // holding a java.time value class across composition.
    var nowEpochSecond by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowEpochSecond = System.currentTimeMillis() / 1000L
            // Sleep until the minute rolls over, then recompute once.
            delay(millisUntilNextMinute())
        }
    }

    val languageTag = LocalizationRegistry.DEFAULT_LANGUAGE
    val localization = LocalizationRegistry.localization(languageTag)
    val vocabulary = LocalizationRegistry.vocabulary(languageTag)
    val grid = LocalizationRegistry.gridOrNull(languageTag)

    val engine = remember { TimeExpressionEngine() }
    val semanticTime = remember(nowEpochSecond) {
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
        LiveWordGridPreview(
            grid = grid,
            localization = localization,
            vocabulary = vocabulary,
            semanticTime = semanticTime,
        )
        Spacer(Modifier.height(24.dp))
        AddWidgetButton()
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.main_add_hint),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
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
    Button(
        onClick = {
            val manager = AppWidgetManager.getInstance(context)
            if (manager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, WordClockWidgetReceiver::class.java)
                manager.requestPinAppWidget(provider, null, null)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.main_add_hint),
                    Toast.LENGTH_LONG,
                ).show()
            }
        },
    ) {
        Text(stringResource(R.string.main_add_widget_button))
    }
}

/**
 * The live word-clock preview: the English 5x5 grid with the current time's
 * words lit in `onBackground` and the rest dimmed in `onSurfaceVariant`.
 *
 * Falls back to the spoken phrase when a language has no grid, exactly like
 * the widget itself does.
 */
@Composable
private fun LiveWordGridPreview(
    grid: WordGrid?,
    localization: TimeLocalization,
    vocabulary: WordVocabulary,
    semanticTime: SemanticTime,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val spoken = localization.format(semanticTime)
        val gridWords = localization.gridWords(semanticTime)

        if (grid != null && gridWords != null) {
            val active = gridWords.toSet()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                grid.rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        row.forEach { key ->
                            val lit = key != null && key in active
                            Text(
                                text = key?.let(vocabulary::word) ?: "",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (lit) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = spoken,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    WordClockTheme {
        Surface { HomeScreen() }
    }
}
