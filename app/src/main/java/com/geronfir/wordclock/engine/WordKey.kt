package com.geronfir.wordclock.engine

/**
 * Every distinct word a word clock can display.
 *
 * Localizations map each key to the language's own spelling, and grids are built
 * from these keys. Keys are intentionally language-neutral: a language that needs
 * extra words adds its own keys rather than overloading an English one.
 *
 * Note that [FIVE] and [TEN] serve double duty (minute unit *and* hour word), which
 * mirrors how physical word clocks share letters between rows.
 */
enum class WordKey {
    // Connectors / structure
    IT_IS,

    // Minute units
    FIVE,
    TEN,
    QUARTER,
    TWENTY,
    HALF,

    // Direction
    PAST,
    TO,
    OCLOCK,

    // Hour words (FIVE and TEN reuse the keys above)
    ONE,
    TWO,
    THREE,
    FOUR,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    ELEVEN,
    TWELVE,

    // Day period (12-hour)
    AM,
    PM,

    // Day period (24-hour / descriptive)
    IN_THE_MORNING,
    IN_THE_AFTERNOON,
    IN_THE_EVENING,
    AT_NIGHT,
}
