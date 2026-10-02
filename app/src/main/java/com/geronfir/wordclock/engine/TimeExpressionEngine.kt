package com.geronfir.wordclock.engine

import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId

/**
 * Converts a clock time into a [SemanticTime].
 *
 * Pure Kotlin + java.time: no Android, no UI, no I/O. That is what makes it
 * testable in a plain JVM unit test (see `TimeExpressionEngineTest`).
 */
class TimeExpressionEngine(private val config: TimeConfig = TimeConfig()) {

    /** Current device time, honouring the device timezone (or an explicit one). */
    fun expressionNow(
        zone: ZoneId = ZoneId.systemDefault(),
        clock: Clock = Clock.system(zone),
    ): SemanticTime {
        val now = LocalTime.now(clock.withZone(zone))
        return expressionAt(now.hour, now.minute)
    }

    fun expressionFor(time: LocalTime): SemanticTime = expressionAt(time.hour, time.minute)

    fun expressionAt(hour24: Int, minute: Int): SemanticTime {
        require(hour24 in 0..23) { "hour24 must be 0..23, was $hour24" }
        require(minute in 0..59) { "minute must be 0..59, was $minute" }

        val rounded = if (config.roundToNearestFive) roundToNearestFive(minute) else minute
        val hourCarry = if (rounded == 60) 1 else 0
        val snappedMinute = if (rounded == 60) 0 else rounded

        val baseHour = (hour24 + hourCarry) % 24

        // "to" expressions point at the *next* hour: 07:35 -> eight.
        val expressionType = when {
            snappedMinute == 0 -> TimeExpressionType.O_CLOCK
            snappedMinute <= 30 -> TimeExpressionType.PAST
            else -> TimeExpressionType.TO
        }
        val phraseHour = if (expressionType == TimeExpressionType.TO) (baseHour + 1) % 24 else baseHour
        val minuteUnit = minuteUnitFor(snappedMinute)

        val words = buildList {
            add(WordKey.IT_IS)
            if (expressionType == TimeExpressionType.O_CLOCK) {
                // "IT IS TWELVE O'CLOCK" — the hour precedes the O'CLOCK marker.
                add(hourWord(phraseHour))
                add(WordKey.OCLOCK)
            } else {
                addAll(minuteUnitWords(minuteUnit))
                add(if (expressionType == TimeExpressionType.PAST) WordKey.PAST else WordKey.TO)
                add(hourWord(phraseHour))
            }
            if (config.includeDayPeriod) add(dayPeriodWord(baseHour))
        }

        return SemanticTime(
            hour24 = hour24,
            minute = minute,
            roundedMinute = snappedMinute,
            phraseHour12 = toHour12(phraseHour),
            expressionType = expressionType,
            minuteUnit = minuteUnit,
            dayPeriod = dayPeriodFor(baseHour),
            use24Hour = config.use24Hour,
            activeWords = words,
        )
    }

    private fun roundToNearestFive(minute: Int): Int {
        val snapped = ((minute + 2) / 5) * 5
        return if (snapped >= 60) 60 else snapped
    }

    private fun minuteUnitFor(snappedMinute: Int): MinuteUnit = when (snappedMinute) {
        0 -> MinuteUnit.NONE
        5 -> MinuteUnit.FIVE
        10 -> MinuteUnit.TEN
        15 -> MinuteUnit.QUARTER
        20 -> MinuteUnit.TWENTY
        25 -> MinuteUnit.TWENTY_FIVE
        30 -> MinuteUnit.HALF
        35 -> MinuteUnit.TWENTY_FIVE
        40 -> MinuteUnit.TWENTY
        45 -> MinuteUnit.QUARTER
        50 -> MinuteUnit.TEN
        55 -> MinuteUnit.FIVE
        else -> MinuteUnit.NONE
    }

    private fun minuteUnitWords(unit: MinuteUnit): List<WordKey> = when (unit) {
        MinuteUnit.NONE -> emptyList()
        MinuteUnit.FIVE -> listOf(WordKey.FIVE)
        MinuteUnit.TEN -> listOf(WordKey.TEN)
        MinuteUnit.QUARTER -> listOf(WordKey.QUARTER)
        MinuteUnit.TWENTY -> listOf(WordKey.TWENTY)
        MinuteUnit.TWENTY_FIVE -> listOf(WordKey.TWENTY, WordKey.FIVE)
        MinuteUnit.HALF -> listOf(WordKey.HALF)
    }

    private fun hourWord(hour24: Int): WordKey = when (toHour12(hour24)) {
        1 -> WordKey.ONE
        2 -> WordKey.TWO
        3 -> WordKey.THREE
        4 -> WordKey.FOUR
        5 -> WordKey.FIVE
        6 -> WordKey.SIX
        7 -> WordKey.SEVEN
        8 -> WordKey.EIGHT
        9 -> WordKey.NINE
        10 -> WordKey.TEN
        11 -> WordKey.ELEVEN
        else -> WordKey.TWELVE
    }

    private fun dayPeriodFor(hour24: Int): DayPeriod = when (hour24) {
        in 5..11 -> DayPeriod.MORNING
        in 12..16 -> DayPeriod.AFTERNOON
        in 17..20 -> DayPeriod.EVENING
        else -> DayPeriod.NIGHT
    }

    private fun dayPeriodWord(hour24: Int): WordKey =
        if (config.use24Hour) {
            when (dayPeriodFor(hour24)) {
                DayPeriod.MORNING -> WordKey.IN_THE_MORNING
                DayPeriod.AFTERNOON -> WordKey.IN_THE_AFTERNOON
                DayPeriod.EVENING -> WordKey.IN_THE_EVENING
                DayPeriod.NIGHT -> WordKey.AT_NIGHT
            }
        } else {
            if (hour24 < 12) WordKey.AM else WordKey.PM
        }

    private fun toHour12(hour24: Int): Int {
        val h = hour24 % 12
        return if (h == 0) 12 else h
    }
}
