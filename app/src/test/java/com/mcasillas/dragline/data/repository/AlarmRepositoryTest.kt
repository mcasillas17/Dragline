package com.mcasillas.dragline.data.repository

import com.mcasillas.dragline.data.local.converters.AlarmMapper
import com.mcasillas.dragline.data.local.dao.AlarmDao
import com.mcasillas.dragline.data.local.entity.AlarmEntity
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.Schedule
import com.mcasillas.dragline.domain.model.SoundSource
import com.mcasillas.dragline.domain.model.WakeChallengeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

class FakeAlarmDao : AlarmDao {
    private val alarms = MutableStateFlow<Map<Long, AlarmEntity>>(emptyMap())
    private var nextId = 1L

    override fun getAllAlarmsFlow(): Flow<List<AlarmEntity>> {
        return alarms.map { it.values.toList() }
    }

    override fun getActiveAlarmsFlow(): Flow<List<AlarmEntity>> {
        return alarms.map { map -> map.values.filter { it.isEnabled } }
    }

    override suspend fun getActiveAlarms(): List<AlarmEntity> {
        return alarms.value.values.filter { it.isEnabled }
    }

    override suspend fun getAlarmById(id: Long): AlarmEntity? {
        return alarms.value[id]
    }

    override suspend fun getNextActiveAlarm(): AlarmEntity? {
        return alarms.value.values
            .filter { it.isEnabled && it.nextTriggerEpochMs > 0 }
            .minByOrNull { it.nextTriggerEpochMs }
    }

    override suspend fun insertAlarm(alarm: AlarmEntity): Long {
        val id = if (alarm.id == 0L) nextId++ else alarm.id
        val entityWithId = alarm.copy(id = id)
        alarms.update { it + (id to entityWithId) }
        return id
    }

    override suspend fun updateAlarm(alarm: AlarmEntity) {
        alarms.update { it + (alarm.id to alarm) }
    }

    override suspend fun deleteAlarmById(id: Long) {
        alarms.update { it - id }
    }

    override suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean) {
        alarms.update { map ->
            val existing = map[id] ?: return@update map
            map + (id to existing.copy(isEnabled = isEnabled))
        }
    }

    override suspend fun updateNextTrigger(id: Long, nextTriggerEpochMs: Long) {
        alarms.update { map ->
            val existing = map[id] ?: return@update map
            map + (id to existing.copy(nextTriggerEpochMs = nextTriggerEpochMs))
        }
    }
}

class AlarmRepositoryTest {

    private lateinit var fakeAlarmDao: FakeAlarmDao
    private lateinit var repository: AlarmRepositoryImpl

    @Before
    fun setup() {
        fakeAlarmDao = FakeAlarmDao()
        repository = AlarmRepositoryImpl(fakeAlarmDao)
    }

    @Test
    fun `insertAlarm stores alarm and retrieves it by id`() = runBlocking {
        val alarm = Alarm(
            label = "Morning Acoustic",
            isEnabled = true,
            schedule = Schedule.FixedTime(LocalTime.of(7, 30), DayOfWeek.WEEKDAYS),
            soundSource = SoundSource.LocalSound("beacon", "Gentle Beacon"),
            wakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
            nextTriggerEpochMs = 1770000000000L
        )

        val generatedId = repository.insertAlarm(alarm)
        assertTrue(generatedId > 0)

        val retrieved = repository.getAlarmById(generatedId)
        assertNotNull(retrieved)
        assertEquals("Morning Acoustic", retrieved?.label)
        assertTrue(retrieved?.isEnabled == true)
        assertEquals(LocalTime.of(7, 30), (retrieved?.schedule as? Schedule.FixedTime)?.time)
        assertEquals(DayOfWeek.WEEKDAYS, retrieved?.schedule?.repeatDays)
    }

    @Test
    fun `setAlarmEnabled updates enabled state in repository`() = runBlocking {
        val alarm = Alarm(
            label = "Test Alarm",
            isEnabled = true,
            schedule = Schedule.FixedTime(LocalTime.of(8, 0))
        )
        val id = repository.insertAlarm(alarm)

        repository.setAlarmEnabled(id, false)

        val updated = repository.getAlarmById(id)
        assertNotNull(updated)
        assertFalse(updated!!.isEnabled)
    }

    @Test
    fun `deleteAlarm removes alarm from repository`() = runBlocking {
        val alarm = Alarm(label = "To Delete")
        val id = repository.insertAlarm(alarm)

        assertNotNull(repository.getAlarmById(id))
        repository.deleteAlarm(id)
        assertNull(repository.getAlarmById(id))
    }

    @Test
    fun `alarm mapper correctly maps between domain and entity with Spotify source`() {
        val domain = Alarm(
            id = 5L,
            label = "Spotify Sunrise",
            isEnabled = true,
            schedule = Schedule.Sunrise(offsetMinutes = 15, repeatDays = DayOfWeek.WEEKENDS),
            soundSource = SoundSource.SpotifyPlaylist("sp_123", "Chill Morning", "spotify:playlist:123"),
            localFallbackSound = "resonance",
            wakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
            nextTriggerEpochMs = 1800000000000L
        )

        val entity = AlarmMapper.toEntity(domain)
        val mappedBack = AlarmMapper.toDomain(entity)

        assertEquals(domain.id, mappedBack.id)
        assertEquals(domain.label, mappedBack.label)
        assertEquals(domain.isEnabled, mappedBack.isEnabled)
        assertTrue(mappedBack.schedule is Schedule.Sunrise)
        assertEquals(15, (mappedBack.schedule as Schedule.Sunrise).offsetMinutes)
        assertEquals(DayOfWeek.WEEKENDS, mappedBack.schedule.repeatDays)
        assertTrue(mappedBack.soundSource is SoundSource.SpotifyPlaylist)
        assertEquals("sp_123", mappedBack.soundSource.id)
        assertEquals("resonance", mappedBack.localFallbackSound)
    }
}
