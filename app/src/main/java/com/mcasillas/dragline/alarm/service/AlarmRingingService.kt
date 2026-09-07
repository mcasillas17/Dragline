package com.mcasillas.dragline.alarm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.mcasillas.dragline.alarm.AlarmNotificationManager
import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.audio.AudioPlayer
import com.mcasillas.dragline.audio.LocalSoundProvider
import com.mcasillas.dragline.audio.PlaybackResult
import com.mcasillas.dragline.audio.SoundProvider
import com.mcasillas.dragline.audio.VibratorHelper
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.model.SoundSource
import com.mcasillas.dragline.domain.model.Track
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.domain.repository.AlarmRepository
import com.mcasillas.dragline.domain.repository.SettingsRepository
import com.mcasillas.dragline.domain.usecase.CalculateNextOccurrenceUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveRingingState(
    val alarmId: Long,
    val label: String,
    val soundSourceDisplayName: String,
    val isFallbackActive: Boolean,
    val wakeChallengeType: WakeChallengeType
)

@AndroidEntryPoint
class AlarmRingingService : Service() {

    @Inject
    lateinit var notificationManager: AlarmNotificationManager

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var calculateNextOccurrenceUseCase: CalculateNextOccurrenceUseCase

    @Inject
    lateinit var audioPlayer: AudioPlayer

    @Inject
    lateinit var vibratorHelper: VibratorHelper

    @Inject
    lateinit var localSoundProvider: LocalSoundProvider

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarm: Alarm? = null

    companion object {
        const val ACTION_START_RINGING = "com.mcasillas.dragline.ACTION_START_RINGING"
        const val ACTION_DISMISS_ALARM = "com.mcasillas.dragline.ACTION_DISMISS_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"

        private val _ringingState = MutableStateFlow<ActiveRingingState?>(null)
        val ringingState: StateFlow<ActiveRingingState?> = _ringingState.asStateFlow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)

        when (action) {
            ACTION_START_RINGING -> {
                if (alarmId != -1L) {
                    startRinging(alarmId)
                }
            }
            ACTION_DISMISS_ALARM -> {
                dismissAlarm(alarmId)
            }
        }

        return START_STICKY
    }

    private fun startRinging(alarmId: Long) {
        acquireWakeLock()

        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: Alarm(
                id = alarmId,
                label = "Alarm",
                isEnabled = true
            )
            currentAlarm = alarm

            // Display high priority foreground notification
            val notification = notificationManager.buildRingingNotification(alarm)
            startForeground(AlarmNotificationManager.NOTIFICATION_ID, notification)

            val settings = settingsRepository.getSettings().first()
            val rampSeconds = if (alarm.isGradualVolumeEnabled) {
                settings.gradualVolumeDurationSeconds
            } else {
                0
            }

            // Audio playback with reliability fallback
            var isFallback = false
            var sourceName = alarm.soundSource.displayName

            if (alarm.soundSource is SoundSource.SpotifyPlaylist) {
                // Streaming not connected yet, trigger fallback to guaranteed local sound
                isFallback = true
                sourceName = "${alarm.soundSource.displayName} (Fallback to Local)"
                val rawRes = localSoundProvider.getLocalTrackResource(alarm.localFallbackSound)
                audioPlayer.playRaw(rawRes, rampSeconds)
            } else {
                val rawRes = localSoundProvider.getLocalTrackResource(alarm.localFallbackSound)
                audioPlayer.playRaw(rawRes, rampSeconds)
            }

            // Vibration
            if (alarm.isVibrationEnabled && settings.isVibrationEnabled) {
                vibratorHelper.startAlarmVibration()
            }

            _ringingState.value = ActiveRingingState(
                alarmId = alarm.id,
                label = alarm.label,
                soundSourceDisplayName = sourceName,
                isFallbackActive = isFallback,
                wakeChallengeType = alarm.wakeChallengeType
            )
        }
    }

    private fun dismissAlarm(alarmId: Long) {
        serviceScope.launch {
            audioPlayer.stop()
            vibratorHelper.stop()
            _ringingState.value = null

            val alarm = currentAlarm ?: alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                if (alarm.isRepeating) {
                    // Recalculate next occurrence and reschedule
                    val nextTrigger = calculateNextOccurrenceUseCase.execute(alarm.schedule)
                    val updated = alarm.copy(nextTriggerEpochMs = nextTrigger)
                    alarmRepository.updateAlarm(updated)
                    alarmScheduler.schedule(updated)
                } else {
                    // One-off alarm: disable it
                    alarmRepository.setAlarmEnabled(alarm.id, false)
                    alarmScheduler.cancel(alarm.id)
                }
            }

            releaseWakeLock()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "dragline:ringing_wake_lock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes safety timeout
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        vibratorHelper.stop()
        releaseWakeLock()
        _ringingState.value = null
    }
}
