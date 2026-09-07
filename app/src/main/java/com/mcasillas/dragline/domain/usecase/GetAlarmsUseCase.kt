package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> {
        return alarmRepository.getAlarms()
    }
}
