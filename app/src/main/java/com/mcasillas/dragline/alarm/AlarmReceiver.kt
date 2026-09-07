package com.mcasillas.dragline.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mcasillas.dragline.alarm.service.AlarmRingingService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_START_RINGING
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
