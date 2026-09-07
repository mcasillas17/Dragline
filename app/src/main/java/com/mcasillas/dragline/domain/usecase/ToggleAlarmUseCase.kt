package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.domain.repository.AlarmRepository
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val calculateNextOccurrenceUseCase: CalculateNextOccurrenceUseCase
) {
    suspend operator fun invoke(alarmId: Long, enabled: Boolean) {
        val alarm = alarmRepository.getAlarmById(alarmId) ?: return

        if (enabled) {
            val nextTrigger = calculateNextOccurrenceUseCase.execute(alarm.schedule)
            val updated = alarm.copy(isEnabled = true, nextTriggerEpochMs = nextTrigger)
            alarmRepository.updateAlarm(updated)
            alarmScheduler.schedule(updated)
        } else {
            val updated = alarm.copy(isEnabled = false, nextTriggerEpochMs = 0L)
            alarmRepository.updateAlarm(updated)
            alarmScheduler.cancel(alarmId)
        }
    }
}
