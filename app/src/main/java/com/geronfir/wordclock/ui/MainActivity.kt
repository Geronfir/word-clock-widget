package com.geronfir.wordclock.ui

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import com.geronfir.wordclock.R

/**
 * Launcher entry point.
 *
 * This app is a home-screen widget, so its "main" screen is just an explanation of
 * how to add the widget. Having a launcher activity is what puts an icon in the app
 * drawer and lets the user open the app at all.
 *
 * Plain [Activity] + programmatic views (no Compose) — deliberately identical in
 * style to [com.geronfir.wordclock.config.WidgetConfigActivity] and dependency-free.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24))
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 24f
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.main_description)
            textSize = 16f
            setPadding(0, dp(12), 0, 0)
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.main_add_hint)
            textSize = 16f
            setPadding(0, dp(16), 0, 0)
        })

        setContentView(ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            addView(root)
        })
    }

    private companion object {
        const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
