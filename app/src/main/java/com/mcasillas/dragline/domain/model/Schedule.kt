package com.mcasillas.dragline.domain.model

import java.time.LocalTime

sealed interface Schedule {
    val repeatDays: Set<DayOfWeek>

    /**
     * Standard fixed time schedule (e.g. 07:00 every weekday).
     */
    data class FixedTime(
        val time: LocalTime,
        override val repeatDays: Set<DayOfWeek> = emptySet()
    ) : Schedule

    /**
     * Contextual schedule based on local sunrise with an optional offset in minutes.
     * Ready for future implementation with location & sunrise calculations.
     */
    data class Sunrise(
        val offsetMinutes: Int = 0,
        override val repeatDays: Set<DayOfWeek> = emptySet()
    ) : Schedule

    /**
     * Contextual schedule relative to the user's first calendar event of the day.
     * Ready for future implementation with Calendar provider access.
     */
    data class FirstMeeting(
        val offsetMinutes: Int = -30,
        override val repeatDays: Set<DayOfWeek> = emptySet()
    ) : Schedule
}
