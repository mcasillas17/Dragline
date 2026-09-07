package com.mcasillas.dragline.scheduling

import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.Schedule
import java.time.ZonedDateTime

class FixedTimeCalculator : ScheduleCalculator<Schedule.FixedTime> {

    override fun calculateNextTrigger(
        schedule: Schedule.FixedTime,
        fromDateTime: ZonedDateTime
    ): ZonedDateTime {
        val targetTime = schedule.time
        val repeatDays = schedule.repeatDays

        // Scenario 1: One-off alarm (no repeat days specified)
        if (repeatDays.isEmpty()) {
            val candidateToday = fromDateTime.with(targetTime).withSecond(0).withNano(0)
            return if (candidateToday.isAfter(fromDateTime)) {
                candidateToday
            } else {
                candidateToday.plusDays(1)
            }
        }

        // Scenario 2: Repeating alarm for specific days of week
        // Check today first, then next 1..7 days
        val currentDayOfWeek = DayOfWeek.fromJavaDayOfWeek(fromDateTime.dayOfWeek)
        val candidateToday = fromDateTime.with(targetTime).withSecond(0).withNano(0)

        if (repeatDays.contains(currentDayOfWeek) && candidateToday.isAfter(fromDateTime)) {
            return candidateToday
        }

        for (dayOffset in 1L..7L) {
            val candidateDay = fromDateTime.plusDays(dayOffset)
            val candidateDayOfWeek = DayOfWeek.fromJavaDayOfWeek(candidateDay.dayOfWeek)
            if (repeatDays.contains(candidateDayOfWeek)) {
                return candidateDay.with(targetTime).withSecond(0).withNano(0)
            }
        }

        // Fallback (e.g. if loop finished without match, fallback to tomorrow at target time)
        return candidateToday.plusDays(1)
    }
}
