package com.mcasillas.dragline.domain.model

import java.time.LocalTime

data class Alarm(
    val id: Long = 0L,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val schedule: Schedule = Schedule.FixedTime(LocalTime.of(7, 0)),
    val soundSource: SoundSource = SoundSource.LocalSound(),
    val localFallbackSound: String = "beacon",
    val wakeChallengeType: WakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
    val isVibrationEnabled: Boolean = true,
    val isGradualVolumeEnabled: Boolean = true,
    val nextTriggerEpochMs: Long = 0L
) {
    val isRepeating: Boolean
        get() = schedule.repeatDays.isNotEmpty()

    val fixedTime: LocalTime?
        get() = (schedule as? Schedule.FixedTime)?.time
}
