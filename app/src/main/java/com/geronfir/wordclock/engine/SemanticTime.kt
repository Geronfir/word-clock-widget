package com.geronfir.wordclock.engine

/** How the minute part of the phrase is expressed. */
enum class TimeExpressionType { O_CLOCK, PAST, TO }

/** The minute chunk a word clock can express (multiples of five). */
enum class MinuteUnit { NONE, FIVE, TEN, QUARTER, TWENTY, TWENTY_FIVE, HALF }

/** Rough part of day, used for the optional day-period suffix. */
enum class DayPeriod { MORNING, AFTERNOON, EVENING, NIGHT }

/**
 * User-facing knobs that affect the semantic output. Kept separate from the widget
 * so the engine stays UI-free and trivially testable.
 */
data class TimeConfig(
    /** 12-hour phrasing (AM/PM) versus 24-hour phrasing. */
    val use24Hour: Boolean = false,
    /** Word clocks normally snap to the nearest five minutes; keep the hook explicit. */
    val roundToNearestFive: Boolean = true,
    /** Append a day-period suffix (AM/PM or "in the morning"). Off by default. */
    val includeDayPeriod: Boolean = false,
)

/**
 * Structured result of converting a time into words.
 *
 * This is deliberately *not* a formatted string: renderers (word grid, flowing
 * text, future styles) all consume this model, so time logic is never duplicated.
 *
 * @param hour24 the real hour the time came from (0..23).
 * @param minute the real minute the time came from (0..59).
 * @param roundedMinute the minute after snapping (0, 5, ... 55).
 * @param phraseHour12 the 1..12 hour the *phrase* refers to. For "to" expressions
 *   this is the following hour, e.g. 07:35 -> 8 ("twenty five to eight").
 * @param activeWords the words to light up, in reading order.
 */
data class SemanticTime(
    val hour24: Int,
    val minute: Int,
    val roundedMinute: Int,
    val phraseHour12: Int,
    val expressionType: TimeExpressionType,
    val minuteUnit: MinuteUnit,
    val dayPeriod: DayPeriod,
    val use24Hour: Boolean,
    val activeWords: List<WordKey>,
) {
    /** True when the phrase carries a day-period suffix. */
    val hasDayPeriod: Boolean
        get() = activeWords.any { it in DAY_PERIOD_WORDS }

    companion object {
        private val DAY_PERIOD_WORDS = setOf(
            WordKey.AM, WordKey.PM,
            WordKey.IN_THE_MORNING, WordKey.IN_THE_AFTERNOON,
            WordKey.IN_THE_EVENING, WordKey.AT_NIGHT,
        )
    }
}
