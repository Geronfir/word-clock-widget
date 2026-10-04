package com.geronfir.wordclock.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.geronfir.wordclock.R
import com.geronfir.wordclock.engine.LocalizationRegistry
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.engine.SemanticTime
import com.geronfir.wordclock.engine.TimeExpressionEngine
import com.geronfir.wordclock.settings.ThemePreset
import com.geronfir.wordclock.settings.WidgetSettings
import com.geronfir.wordclock.settings.WidgetSettingsStore
import com.geronfir.wordclock.ui.components.WordClockPreview
import com.geronfir.wordclock.ui.theme.WordClockTheme
import com.geronfir.wordclock.widget.WordClockWidget
import com.geronfir.wordclock.widget.WordClockWidgetReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Word clocks change at most once a minute, so wake up on the minute boundary. */
private const val MILLIS_PER_MINUTE = 60_000L

private fun millisUntilNextMinute(): Long =
    MILLIS_PER_MINUTE - (System.currentTimeMillis() % MILLIS_PER_MINUTE)

/** Widget ids currently placed on the home screen, in the launcher's order. */
private fun installedWidgetIds(context: Context): IntArray =
    runCatching {
        AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, WordClockWidgetReceiver::class.java),
        )
    }.getOrDefault(IntArray(0))

/**
 * Survives a configuration change (rotation, locale, font scale) so an edit the
 * user has not applied yet is not silently thrown away. Without this the screen
 * would reload the last *saved* settings and lose the pending selection.
 */
private val WidgetSettingsSaver = listSaver<WidgetSettings, Any>(
    save = {
        listOf(
            it.languageTag,
            it.use24Hour,
            it.includeDayPeriod,
            it.representationStyle.name,
            it.activeColorArgb,
            it.inactiveColorArgb,
            it.backgroundColorArgb,
            it.fontScale,
        )
    },
    restore = {
        WidgetSettings(
            languageTag = it[0] as String,
            use24Hour = it[1] as Boolean,
            includeDayPeriod = it[2] as Boolean,
            representationStyle = RepresentationStyle.valueOf(it[3] as String),
            activeColorArgb = it[4] as Long,
            inactiveColorArgb = it[5] as Long,
            backgroundColorArgb = it[6] as Long,
            fontScale = it[7] as Float,
        )
    },
)

/**
 * Display: everything the widget can be configured with, in Material 3.
 *
 * The widget's own configuration screen ([com.geronfir.wordclock.config.WidgetConfigActivity])
 * still exists — Android shows it when a widget is first placed. This screen is
 * the in-app equivalent, and it applies the result to **every** placed widget,
 * which is the only behaviour that makes sense from inside the app (there is no
 * `appWidgetId` in scope here). The preview at the top updates as the user
 * changes each control, before anything is written.
 */
@Composable
fun DisplayScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val store = remember(context) { WidgetSettingsStore(context) }

    var settings by rememberSaveable(stateSaver = WidgetSettingsSaver) {
        mutableStateOf(WidgetSettings.DEFAULT)
    }
    var loaded by rememberSaveable { mutableStateOf(false) }

    // Seed the controls from the first placed widget so the screen opens on the
    // values the user is actually looking at on their home screen.
    LaunchedEffect(Unit) {
        if (!loaded) {
            val ids = installedWidgetIds(context)
            if (ids.isNotEmpty()) {
                settings = runCatching { store.load(ids.first()) }
                    .getOrDefault(WidgetSettings.DEFAULT)
            }
            loaded = true
        }
    }

    // Live clock, same minute-boundary loop the Home preview uses.
    var nowEpochSecond by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowEpochSecond = System.currentTimeMillis() / 1000L
            delay(millisUntilNextMinute())
        }
    }
    val engine = remember { TimeExpressionEngine() }
    val semanticTime = remember(nowEpochSecond) {
        val now = java.time.Instant.ofEpochSecond(nowEpochSecond)
            .atZone(java.time.ZoneId.systemDefault())
        engine.expressionAt(now.hour, now.minute)
    }

    val hasGrid = LocalizationRegistry.gridOrNull(settings.languageTag) != null

    // Resolved at composable level (not inside the click lambda): lint's
    // LocalContextGetResourceValueCall, and they re-resolve on a config change.
    // The applied count is a trigger: setting it recomposes, the message is
    // built here where string resources are legal, and the effect shows it.
    val noWidgetsMessage = stringResource(R.string.display_no_widgets)
    var appliedCount by remember { mutableStateOf<Int?>(null) }
    val appliedMessage = appliedCount?.let {
        pluralStringResource(R.plurals.display_applied, it, it)
    }
    LaunchedEffect(appliedCount) {
        if (appliedMessage != null) {
            snackbarHostState.showSnackbar(appliedMessage)
            appliedCount = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.nav_display),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(16.dp))

            WordClockPreview(settings = settings, semanticTime = semanticTime)

            SectionTitle(R.string.display_language)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LocalizationRegistry.selectableLanguageTags.forEachIndexed { index, tag ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = LocalizationRegistry.selectableLanguageTags.size,
                        ),
                        onClick = {
                            // A language without a grid can never render the word
                            // grid, so switching to one also moves the style across
                            // instead of leaving an impossible combination selected.
                            val newHasGrid = LocalizationRegistry.gridOrNull(tag) != null
                            settings = settings.copy(
                                languageTag = tag,
                                representationStyle = if (newHasGrid) {
                                    settings.representationStyle
                                } else {
                                    RepresentationStyle.FLOWING_TEXT
                                },
                            )
                        },
                        selected = settings.languageTag == tag,
                    ) {
                        Text(languageLabel(tag))
                    }
                }
            }

            SectionTitle(R.string.display_style)
            val styles = RepresentationStyle.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                styles.forEachIndexed { index, style ->
                    val isGridOption = style == RepresentationStyle.WORD_GRID
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = styles.size,
                        ),
                        onClick = { settings = settings.copy(representationStyle = style) },
                        selected = settings.representationStyle == style,
                        // Greyed out rather than silently ignored: the user can see
                        // *why* the word grid is unavailable for this language.
                        enabled = !isGridOption || hasGrid,
                    ) {
                        Text(styleLabel(style))
                    }
                }
            }
            if (!hasGrid) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.display_style_grid_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionTitle(R.string.display_format)
            SwitchRow(
                label = stringResource(R.string.config_use_24_hour),
                checked = settings.use24Hour,
                onCheckedChange = { settings = settings.copy(use24Hour = it) },
            )
            SwitchRow(
                label = stringResource(R.string.config_show_day_period),
                checked = settings.includeDayPeriod,
                onCheckedChange = { settings = settings.copy(includeDayPeriod = it) },
            )

            SectionTitle(R.string.display_font_size)
            val scales = WidgetSettings.FONT_SCALES
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                scales.forEachIndexed { index, scale ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = scales.size),
                        onClick = { settings = settings.copy(fontScale = scale) },
                        selected = kotlin.math.abs(settings.fontScale - scale) < 0.001f,
                    ) {
                        Text(fontLabel(scale))
                    }
                }
            }

            SectionTitle(R.string.display_colors)
            val presets = ThemePreset.entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { preset ->
                            FilterChip(
                                selected = ThemePreset.matching(settings) == preset,
                                onClick = { settings = ThemePreset.apply(preset, settings) },
                                label = { Text(stringResource(preset.labelRes)) },
                                leadingIcon = { ThemeSwatch(preset) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        val ids = installedWidgetIds(context)
                        if (ids.isEmpty()) {
                            snackbarHostState.showSnackbar(noWidgetsMessage)
                            return@launch
                        }
                        ids.forEach { id -> store.save(id, settings) }
                        runCatching { WordClockWidget().updateAll(context) }
                        appliedCount = ids.size
                    }
                },
            ) {
                Text(stringResource(R.string.display_apply))
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { settings = WidgetSettings.DEFAULT },
            ) {
                Text(stringResource(R.string.display_reset))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

/** Three tiny colour chips (active / inactive / background) for a preset. */
@Composable
private fun ThemeSwatch(preset: ThemePreset) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(
            preset.activeColorArgb,
            preset.inactiveColorArgb,
            preset.backgroundColorArgb,
        ).forEach { argb ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color(argb.toInt()), RoundedCornerShape(3.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
        }
    }
}

@Composable
private fun SectionTitle(resId: Int) {
    Spacer(Modifier.height(20.dp))
    Text(
        text = stringResource(resId),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun languageLabel(tag: String): String = when (tag) {
    "id" -> "Bahasa Indonesia"
    else -> "English"
}

private fun styleLabel(style: RepresentationStyle): String = when (style) {
    RepresentationStyle.WORD_GRID -> "Word grid"
    else -> "Flowing text"
}

private fun fontLabel(scale: Float): String = when {
    scale <= 0.8f -> "Small"
    scale >= 1.4f -> "Extra large"
    scale >= 1.2f -> "Large"
    else -> "Default"
}

@Preview(showBackground = true)
@Composable
private fun DisplayScreenPreview() {
    WordClockTheme {
        DisplayScreen()
    }
}
