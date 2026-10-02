package com.geronfir.wordclock.config

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import com.geronfir.wordclock.R
import com.geronfir.wordclock.engine.RepresentationStyle
import com.geronfir.wordclock.settings.WidgetSettings
import com.geronfir.wordclock.settings.WidgetSettingsStore
import com.geronfir.wordclock.widget.WordClockWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

/**
 * Configuration screen shown when the widget is placed (and when it is
 * re-configured later). Deliberately minimal: one screen, saved per widget
 * instance, then straight back to the home screen.
 *
 * The activity writes through [WidgetSettingsStore], so settings survive widget
 * lifecycle changes and stay scoped to this `appWidgetId`.
 */
class WidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var store: WidgetSettingsStore
    private lateinit var use24Hour: CheckBox
    private lateinit var includeDayPeriod: CheckBox
    private lateinit var styleGroup: RadioGroup

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

        // Pre-fill with whatever this instance already has.
        CoroutineScope(Dispatchers.Main).launch {
            val current = store.load(appWidgetId)
            use24Hour.isChecked = current.use24Hour
            includeDayPeriod.isChecked = current.includeDayPeriod
            styleGroup.check(
                if (current.representationStyle == RepresentationStyle.FLOWING_TEXT) {
                    STYLE_FLOWING
                } else {
                    STYLE_GRID
                },
            )
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

        use24Hour = CheckBox(this).apply { text = getString(R.string.config_use_24_hour) }
        root.addView(use24Hour)

        includeDayPeriod = CheckBox(this).apply { text = getString(R.string.config_show_day_period) }
        root.addView(includeDayPeriod)

        root.addView(TextView(this).apply {
            text = getString(R.string.config_style)
            setPadding(0, dp(12), 0, dp(4))
        })

        styleGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        styleGroup.addView(RadioButton(this).apply {
            id = STYLE_GRID
            text = getString(R.string.config_style_grid)
        })
        styleGroup.addView(RadioButton(this).apply {
            id = STYLE_FLOWING
            text = getString(R.string.config_style_flowing)
        })
        root.addView(styleGroup)

        root.addView(Button(this).apply {
            text = getString(R.string.config_save)
            setOnClickListener { save() }
        })

        return ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            gravity = Gravity.CENTER_HORIZONTAL
            addView(root)
        }
    }

    private fun save() {
        val settings = WidgetSettings(
            use24Hour = use24Hour.isChecked,
            includeDayPeriod = includeDayPeriod.isChecked,
            representationStyle = if (styleGroup.checkedRadioButtonId == STYLE_FLOWING) {
                RepresentationStyle.FLOWING_TEXT
            } else {
                RepresentationStyle.WORD_GRID
            },
        )

        CoroutineScope(Dispatchers.Main).launch {
            store.save(appWidgetId, settings)
            runCatching { WordClockWidget().updateAll(applicationContext) }

            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private companion object {
        const val STYLE_GRID = 1
        const val STYLE_FLOWING = 2
        const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
