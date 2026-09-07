package com.mcasillas.dragline.scheduling

import com.mcasillas.dragline.domain.model.Schedule
import java.time.ZonedDateTime

interface SunriseScheduleCalculator : ScheduleCalculator<Schedule.Sunrise>

/**
 * Extensible default calculator for Sunrise schedules.
 * Ready to integrate with solar positioning algorithms and Android location services.
 */
class DefaultSunriseScheduleCalculator : SunriseScheduleCalculator {
    override fun calculateNextTrigger(
        schedule: Schedule.Sunrise,
        fromDateTime: ZonedDateTime
    ): ZonedDateTime {
        // Base placeholder sunrise at 06:00 AM + offsetMinutes
        val baseSunrise = fromDateTime
            .withHour(6)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusMinutes(schedule.offsetMinutes.toLong())

        return if (baseSunrise.isAfter(fromDateTime)) {
            baseSunrise
        } else {
            baseSunrise.plusDays(1)
        }
    }
}
