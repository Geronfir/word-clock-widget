package com.geronfir.wordclock.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.geronfir.wordclock.ui.theme.WordClockTheme

/**
 * Launcher entry point.
 *
 * Thin shell: everything visible lives in Compose. [WordClockApp] hosts the
 * Material 3 scaffold (navigation bar + NavHost) and the individual screens.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordClockTheme {
                WordClockApp()
            }
        }
    }
}
