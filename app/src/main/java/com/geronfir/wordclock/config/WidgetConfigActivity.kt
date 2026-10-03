package com.geronfir.wordclock.config

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.glance.appwidget.updateAll
import com.geronfir.wordclock.R
import com.geronfir.wordclock.engine.LocalizationRegistry
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.settings.ThemePreset
import com.geronfir.wordclock.settings.WidgetSettings
import com.geronfir.wordclock.settings.WidgetSettingsStore
import com.geronfir.wordclock.widget.WordClockWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Configuration screen shown when the widget is placed (and when it is
 * re-configured later). One screen, saved per widget instance, then straight back
 * to the home screen.
 *
 * The activity writes through [WidgetSettingsStore], so settings survive widget
 * lifecycle changes and stay scoped to this `appWidgetId`. It always starts from
 * the instance's current settings and mutates a copy, so re-opening the screen
 * never resets a choice the user made earlier.
 */
class WidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var store: WidgetSettingsStore

    // The single source of truth while the screen is open; each control writes a
    // copy of it so nothing else is lost when one field changes.
    private var current: WidgetSettings = WidgetSettings.DEFAULT

    private lateinit var use24Hour: CheckBox
    private lateinit var includeDayPeriod: CheckBox
    private lateinit var languageGroup: RadioGroup
    private lateinit var styleGroup: RadioGroup
    private lateinit var themeGroup: RadioGroup
    private lateinit var fontGroup: RadioGroup

    private val languageIds = mutableListOf<String>()
    private val themeIds = mutableListOf<ThemePreset>()
    private val fontScales = mutableListOf<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        store = WidgetSettingsStore(this)
        setContentView(buildContentView())

        CoroutineScope(Dispatchers.Main).launch {
            current = store.load(appWidgetId)
            bindToControls()
        }
    }

    private fun buildContentView(): ScrollView {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20))
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.config_title)
            textSize = 20f
        })

        // --- Language ---------------------------------------------------------
        root.addView(sectionLabel(R.string.config_language, dp(4)))
        languageGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        languageIds.clear()
        LocalizationRegistry.selectableLanguageTags.forEach { tag ->
            val id = languageIds.size + LANGUAGE_BASE
            languageIds += tag
            languageGroup.addView(RadioButton(this).apply {
                this.id = id
                text = languageLabel(tag)
            })
        }
        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            languageIds.getOrNull(checkedId - LANGUAGE_BASE)?.let {
                current = current.copy(languageTag = it)
            }
        }
        root.addView(languageGroup)

        // --- Format -----------------------------------------------------------
        root.addView(sectionLabel(R.string.config_format, dp(12)))
        use24Hour = CheckBox(this).apply {
            text = getString(R.string.config_use_24_hour)
            setOnCheckedChangeListener { _, v -> current = current.copy(use24Hour = v) }
        }
        root.addView(use24Hour)

        includeDayPeriod = CheckBox(this).apply {
            text = getString(R.string.config_show_day_period)
            setOnCheckedChangeListener { _, v -> current = current.copy(includeDayPeriod = v) }
        }
        root.addView(includeDayPeriod)

        // --- Style ------------------------------------------------------------
        root.addView(sectionLabel(R.string.config_style, dp(12)))
        styleGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        styleGroup.addView(RadioButton(this).apply {
            id = STYLE_GRID
            text = getString(R.string.config_style_grid)
        })
        styleGroup.addView(RadioButton(this).apply {
            id = STYLE_FLOWING
            text = getString(R.string.config_style_flowing)
        })
        styleGroup.setOnCheckedChangeListener { _, checkedId ->
            current = current.copy(
                representationStyle = if (checkedId == STYLE_FLOWING) {
                    RepresentationStyle.FLOWING_TEXT
                } else {
                    RepresentationStyle.WORD_GRID
                },
            )
        }
        root.addView(styleGroup)

        // --- Colours ----------------------------------------------------------
        root.addView(sectionLabel(R.string.config_theme, dp(12)))
        themeGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        themeIds.clear()
        ThemePreset.entries.forEach { preset ->
            val id = themeIds.size + THEME_BASE
            themeIds += preset
            themeGroup.addView(RadioButton(this).apply {
                this.id = id
                text = getString(preset.labelRes)
            })
        }
        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            themeIds.getOrNull(checkedId - THEME_BASE)?.let {
                current = ThemePreset.apply(it, current)
            }
        }
        root.addView(themeGroup)

        // --- Font size --------------------------------------------------------
        root.addView(sectionLabel(R.string.config_font_size, dp(12)))
        fontGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        fontScales.clear()
        WidgetSettings.FONT_SCALES.forEach { scale ->
            val id = fontScales.size + FONT_BASE
            fontScales += scale
            fontGroup.addView(RadioButton(this).apply {
                this.id = id
                text = fontLabel(scale)
            })
        }
        fontGroup.setOnCheckedChangeListener { _, checkedId ->
            fontScales.getOrNull(checkedId - FONT_BASE)?.let {
                current = current.copy(fontScale = it)
            }
        }
        root.addView(fontGroup)

        root.addView(Button(this).apply {
            text = getString(R.string.config_save)
            setOnClickListener { save() }
        })

        return ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            addView(root)
        }
    }

    /** Pushes [current] into the controls (used once the saved settings arrive). */
    private fun bindToControls() {
        use24Hour.isChecked = current.use24Hour
        includeDayPeriod.isChecked = current.includeDayPeriod

        languageIds.indexOf(current.languageTag).takeIf { it >= 0 }
            ?.let { languageGroup.check(it + LANGUAGE_BASE) }

        styleGroup.check(
            if (current.representationStyle == RepresentationStyle.FLOWING_TEXT) STYLE_FLOWING else STYLE_GRID,
        )

        themeGroup.check(ThemePreset.matching(current).ordinal + THEME_BASE)

        val fontIndex = fontScales.indexOfFirst { kotlin.math.abs(it - current.fontScale) < 0.001f }
        fontGroup.check((if (fontIndex >= 0) fontIndex else 1) + FONT_BASE)
    }

    private fun sectionLabel(textRes: Int, topPadding: Int) = TextView(this).apply {
        text = getString(textRes)
        setPadding(0, topPadding, 0, 0)
    }

    private fun languageLabel(tag: String): String = when (tag) {
        "id" -> getString(R.string.config_language_id)
        else -> getString(R.string.config_language_en)
    }

    private fun fontLabel(scale: Float): String = when {
        scale <= 0.8f -> getString(R.string.config_font_small)
        scale >= 1.4f -> getString(R.string.config_font_large)
        scale >= 1.2f -> getString(R.string.config_font_medium)
        else -> getString(R.string.config_font_default)
    }

    private fun save() {
        CoroutineScope(Dispatchers.Main).launch {
            store.save(appWidgetId, current)
            runCatching { WordClockWidget().updateAll(applicationContext) }

            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private companion object {
        const val STYLE_GRID = 1
        const val STYLE_FLOWING = 2

        // Radio-button id bases; each group's ids are offset so they never clash.
        const val LANGUAGE_BASE = 1000
        const val THEME_BASE = 2000
        const val FONT_BASE = 3000

        const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
