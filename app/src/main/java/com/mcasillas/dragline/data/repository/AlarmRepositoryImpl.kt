package com.mcasillas.dragline.data.repository

import com.mcasillas.dragline.data.local.converters.AlarmMapper
import com.mcasillas.dragline.data.local.dao.AlarmDao
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarmsFlow().map { list ->
            list.map { AlarmMapper.toDomain(it) }
        }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.let { AlarmMapper.toDomain(it) }
    }

    override suspend fun getNextActiveAlarm(): Alarm? {
        return alarmDao.getNextActiveAlarm()?.let { AlarmMapper.toDomain(it) }
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        val entity = AlarmMapper.toEntity(alarm)
        return alarmDao.insertAlarm(entity)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val entity = AlarmMapper.toEntity(alarm)
        alarmDao.updateAlarm(entity)
    }

    override suspend fun deleteAlarm(id: Long) {
        alarmDao.deleteAlarmById(id)
    }

    override suspend fun setAlarmEnabled(id: Long, enabled: Boolean) {
        alarmDao.setAlarmEnabled(id, enabled)
    }
}
