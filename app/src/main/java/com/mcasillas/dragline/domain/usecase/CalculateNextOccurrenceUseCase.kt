package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.domain.model.Schedule
import com.mcasillas.dragline.scheduling.FixedTimeCalculator
import com.mcasillas.dragline.scheduling.MeetingScheduleCalculator
import com.mcasillas.dragline.scheduling.SunriseScheduleCalculator
import java.time.ZonedDateTime
import javax.inject.Inject

class CalculateNextOccurrenceUseCase @Inject constructor(
    private val fixedTimeCalculator: FixedTimeCalculator,
    private val sunriseCalculator: SunriseScheduleCalculator,
    private val meetingCalculator: MeetingScheduleCalculator
) {

    fun execute(
        schedule: Schedule,
        fromDateTime: ZonedDateTime = ZonedDateTime.now()
    ): Long {
        val nextZonedDateTime = when (schedule) {
            is Schedule.FixedTime -> fixedTimeCalculator.calculateNextTrigger(schedule, fromDateTime)
            is Schedule.Sunrise -> sunriseCalculator.calculateNextTrigger(schedule, fromDateTime)
            is Schedule.FirstMeeting -> meetingCalculator.calculateNextTrigger(schedule, fromDateTime)
        }
        return nextZonedDateTime.toInstant().toEpochMilli()
    }
}
