package com.mcasillas.dragline.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mcasillas.dragline.domain.repository.AlarmRepository
import com.mcasillas.dragline.domain.usecase.CalculateNextOccurrenceUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var calculateNextOccurrenceUseCase: CalculateNextOccurrenceUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarms = alarmRepository.getAlarms().first()
                    alarms.filter { it.isEnabled }.forEach { alarm ->
                        val nextTrigger = calculateNextOccurrenceUseCase.execute(alarm.schedule)
                        val updated = alarm.copy(nextTriggerEpochMs = nextTrigger)
                        alarmRepository.updateAlarm(updated)
                        alarmScheduler.schedule(updated)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
