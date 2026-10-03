package com.geronfir.wordclock.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.engine.TimeConfig
import kotlinx.coroutines.flow.first

/** Everything a single widget instance can be configured with. */
data class WidgetSettings(
    val languageTag: String = "en",
    val use24Hour: Boolean = false,
    val includeDayPeriod: Boolean = false,
    val representationStyle: RepresentationStyle = RepresentationStyle.WORD_GRID,
    val activeColorArgb: Long = 0xFFE8E8EC,
    val inactiveColorArgb: Long = 0xFF3A3A42,
    val backgroundColorArgb: Long = 0xFF101014,
    /** User font-size multiplier, 1.0 = the size the layout would pick on its own. */
    val fontScale: Float = 1.0f,
) {
    fun toTimeConfig(): TimeConfig = TimeConfig(
        use24Hour = use24Hour,
        includeDayPeriod = includeDayPeriod,
    )

    companion object {
        val DEFAULT = WidgetSettings()

        /** Font-size choices offered in the configuration screen. */
        val FONT_SCALES: List<Float> = listOf(0.75f, 1.0f, 1.25f, 1.5f)
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "word_clock_settings")

/**
 * Per-instance settings storage.
 *
 * Every key is namespaced by the widget's `appWidgetId`, so two widgets on the
 * same home screen can show different languages, formats, colours and font sizes.
 * The spec explicitly forbids one global settings blob; this is the mechanism
 * that keeps them independent.
 *
 * All reads are defensive: a missing or corrupt value falls back to
 * [WidgetSettings.DEFAULT] instead of crashing the widget.
 */
class WidgetSettingsStore(private val context: Context) {

    private fun key(appWidgetId: Int, name: String) = stringPreferencesKey("widget_${appWidgetId}_$name")

    suspend fun load(appWidgetId: Int): WidgetSettings {
        val prefs = runCatching { context.dataStore.data.first() }.getOrNull() ?: return WidgetSettings.DEFAULT
        return runCatching {
            WidgetSettings(
                languageTag = prefs[key(appWidgetId, "language")] ?: WidgetSettings.DEFAULT.languageTag,
                use24Hour = prefs[key(appWidgetId, "use24")]?.toBooleanStrictOrNull() ?: false,
                includeDayPeriod = prefs[key(appWidgetId, "dayPeriod")]?.toBooleanStrictOrNull() ?: false,
                representationStyle = prefs[key(appWidgetId, "style")]
                    ?.let { runCatching { RepresentationStyle.valueOf(it) }.getOrNull() }
                    ?: RepresentationStyle.WORD_GRID,
                activeColorArgb = prefs[key(appWidgetId, "activeColor")]?.toLongOrNull()
                    ?: WidgetSettings.DEFAULT.activeColorArgb,
                inactiveColorArgb = prefs[key(appWidgetId, "inactiveColor")]?.toLongOrNull()
                    ?: WidgetSettings.DEFAULT.inactiveColorArgb,
                backgroundColorArgb = prefs[key(appWidgetId, "bgColor")]?.toLongOrNull()
                    ?: WidgetSettings.DEFAULT.backgroundColorArgb,
                fontScale = prefs[key(appWidgetId, "fontScale")]?.toFloatOrNull()
                    ?.takeIf { it in MIN_FONT_SCALE..MAX_FONT_SCALE } ?: 1.0f,
            )
        }.getOrDefault(WidgetSettings.DEFAULT)
    }

    suspend fun save(appWidgetId: Int, settings: WidgetSettings) {
        context.dataStore.edit { prefs ->
            prefs[key(appWidgetId, "language")] = settings.languageTag
            prefs[key(appWidgetId, "use24")] = settings.use24Hour.toString()
            prefs[key(appWidgetId, "dayPeriod")] = settings.includeDayPeriod.toString()
            prefs[key(appWidgetId, "style")] = settings.representationStyle.name
            prefs[key(appWidgetId, "activeColor")] = settings.activeColorArgb.toString()
            prefs[key(appWidgetId, "inactiveColor")] = settings.inactiveColorArgb.toString()
            prefs[key(appWidgetId, "bgColor")] = settings.backgroundColorArgb.toString()
            prefs[key(appWidgetId, "fontScale")] = settings.fontScale.toString()
        }
    }

    suspend fun delete(appWidgetId: Int) {
        context.dataStore.edit { prefs ->
            val prefix = "widget_${appWidgetId}_"
            prefs.asMap().keys
                .filter { it.name.startsWith(prefix) }
                .forEach { prefs.remove(it) }
        }
    }

    private companion object {
        // Clamp so a corrupt/edited value cannot make the widget unreadable.
        const val MIN_FONT_SCALE = 0.5f
        const val MAX_FONT_SCALE = 3.0f
    }
}
