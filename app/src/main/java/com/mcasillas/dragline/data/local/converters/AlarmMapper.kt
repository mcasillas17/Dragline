package com.mcasillas.dragline.data.local.converters

import com.mcasillas.dragline.data.local.entity.AlarmEntity
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.Schedule
import com.mcasillas.dragline.domain.model.SoundSource
import com.mcasillas.dragline.domain.model.WakeChallengeType
import java.time.LocalTime

object AlarmMapper {

    fun toDomain(entity: AlarmEntity): Alarm {
        val repeatDays = if (entity.repeatDays.isBlank()) {
            emptySet()
        } else {
            entity.repeatDays.split(",")
                .mapNotNull { name ->
                    try {
                        DayOfWeek.valueOf(name.trim())
                    } catch (e: Exception) {
                        null
                    }
                }.toSet()
        }

        val schedule = when (entity.scheduleType) {
            "SUNRISE" -> Schedule.Sunrise(
                offsetMinutes = entity.offsetMinutes,
                repeatDays = repeatDays
            )
            "FIRST_MEETING" -> Schedule.FirstMeeting(
                offsetMinutes = entity.offsetMinutes,
                repeatDays = repeatDays
            )
            else -> Schedule.FixedTime(
                time = LocalTime.of(entity.hour.coerceIn(0, 23), entity.minute.coerceIn(0, 59)),
                repeatDays = repeatDays
            )
        }

        val soundSource = when (entity.soundSourceType) {
            "SPOTIFY" -> SoundSource.SpotifyPlaylist(
                id = entity.soundSourceId,
                displayName = entity.soundSourceDisplayName,
                uri = entity.soundSourceUri,
                isMock = true
            )
            else -> SoundSource.LocalSound(
                id = entity.soundSourceId.ifBlank { "beacon" },
                displayName = entity.soundSourceDisplayName.ifBlank { "Gentle Beacon" }
            )
        }

        val wakeChallenge = try {
            WakeChallengeType.valueOf(entity.wakeChallengeType)
        } catch (e: Exception) {
            WakeChallengeType.HOLD_TO_WAKE
        }

        return Alarm(
            id = entity.id,
            label = entity.label,
            isEnabled = entity.isEnabled,
            schedule = schedule,
            soundSource = soundSource,
            localFallbackSound = entity.localFallbackSound.ifBlank { "beacon" },
            wakeChallengeType = wakeChallenge,
            isVibrationEnabled = entity.isVibrationEnabled,
            isGradualVolumeEnabled = entity.isGradualVolumeEnabled,
            nextTriggerEpochMs = entity.nextTriggerEpochMs
        )
    }

    fun toEntity(domain: Alarm): AlarmEntity {
        val (scheduleType, hour, minute, offsetMinutes) = when (val s = domain.schedule) {
            is Schedule.FixedTime -> Quadruple("FIXED_TIME", s.time.hour, s.time.minute, 0)
            is Schedule.Sunrise -> Quadruple("SUNRISE", 6, 0, s.offsetMinutes)
            is Schedule.FirstMeeting -> Quadruple("FIRST_MEETING", 9, 0, s.offsetMinutes)
        }

        val (sourceType, sourceId, sourceDisplayName, sourceUri) = when (val src = domain.soundSource) {
            is SoundSource.SpotifyPlaylist -> Quadruple("SPOTIFY", src.id, src.displayName, src.uri)
            is SoundSource.LocalSound -> Quadruple("LOCAL", src.id, src.displayName, src.resourceName)
        }

        val repeatDaysString = domain.schedule.repeatDays.joinToString(",") { it.name }

        return AlarmEntity(
            id = domain.id,
            label = domain.label,
            isEnabled = domain.isEnabled,
            scheduleType = scheduleType,
            hour = hour,
            minute = minute,
            offsetMinutes = offsetMinutes,
            repeatDays = repeatDaysString,
            soundSourceType = sourceType,
            soundSourceId = sourceId,
            soundSourceDisplayName = sourceDisplayName,
            soundSourceUri = sourceUri,
            localFallbackSound = domain.localFallbackSound,
            wakeChallengeType = domain.wakeChallengeType.name,
            isVibrationEnabled = domain.isVibrationEnabled,
            isGradualVolumeEnabled = domain.isGradualVolumeEnabled,
            nextTriggerEpochMs = domain.nextTriggerEpochMs
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
