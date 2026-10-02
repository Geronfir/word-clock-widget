package com.geronfir.wordclock.engine

/**
 * The visual style the widget renders. The engine does not care which one is
 * used — this enum exists so settings can select a renderer without the widget
 * hard-coding a single layout.
 */
enum class RepresentationStyle {
    WORD_GRID,
    FLOWING_TEXT,
}
