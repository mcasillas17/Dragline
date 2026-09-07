package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.repository.AlarmRepository
import javax.inject.Inject

class SaveAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val calculateNextOccurrenceUseCase: CalculateNextOccurrenceUseCase
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        val nextTrigger = if (alarm.isEnabled) {
            calculateNextOccurrenceUseCase.execute(alarm.schedule)
        } else {
            0L
        }

        val alarmToSave = alarm.copy(nextTriggerEpochMs = nextTrigger)

        val savedId = if (alarmToSave.id == 0L) {
            alarmRepository.insertAlarm(alarmToSave)
        } else {
            alarmRepository.updateAlarm(alarmToSave)
            alarmToSave.id
        }

        val finalAlarm = alarmToSave.copy(id = savedId)

        if (finalAlarm.isEnabled) {
            alarmScheduler.schedule(finalAlarm)
        } else {
            alarmScheduler.cancel(finalAlarm.id)
        }

        return savedId
    }
}
