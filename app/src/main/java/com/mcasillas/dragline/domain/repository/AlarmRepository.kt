package com.mcasillas.dragline.domain.repository

import com.mcasillas.dragline.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun getNextActiveAlarm(): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Long)
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
}
