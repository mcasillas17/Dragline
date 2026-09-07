package com.mcasillas.dragline.scheduling

import com.mcasillas.dragline.domain.model.Schedule
import java.time.ZonedDateTime

interface ScheduleCalculator<T : Schedule> {
    /**
     * Calculates the next exact occurrence of the schedule relative to [fromDateTime].
     */
    fun calculateNextTrigger(schedule: T, fromDateTime: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime
}
