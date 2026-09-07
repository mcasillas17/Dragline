package com.mcasillas.dragline.scheduling

import com.mcasillas.dragline.domain.model.Schedule
import java.time.ZonedDateTime

interface MeetingScheduleCalculator : ScheduleCalculator<Schedule.FirstMeeting>

/**
 * Extensible default calculator for First Meeting schedules.
 * Ready to integrate with Android Calendar Provider.
 */
class DefaultMeetingScheduleCalculator : MeetingScheduleCalculator {
    override fun calculateNextTrigger(
        schedule: Schedule.FirstMeeting,
        fromDateTime: ZonedDateTime
    ): ZonedDateTime {
        // Base placeholder first meeting at 09:00 AM + offsetMinutes (e.g. -30 min = 08:30 AM)
        val baseMeeting = fromDateTime
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusMinutes(schedule.offsetMinutes.toLong())

        return if (baseMeeting.isAfter(fromDateTime)) {
            baseMeeting
        } else {
            baseMeeting.plusDays(1)
        }
    }
}
