package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.domain.repository.AlarmRepository
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarmId: Long) {
        alarmScheduler.cancel(alarmId)
        alarmRepository.deleteAlarm(alarmId)
    }
}
