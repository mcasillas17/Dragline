package com.mcasillas.dragline.alarm

import com.mcasillas.dragline.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarmId: Long)
    fun canScheduleExactAlarms(): Boolean
}
