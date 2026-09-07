package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.repository.AlarmRepository
import javax.inject.Inject

class GetNextAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(): Alarm? {
        return alarmRepository.getNextActiveAlarm()
    }
}
