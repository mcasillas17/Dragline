package com.mcasillas.dragline.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var volumeRampJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun playRaw(resourceId: Int, gradualDurationSeconds: Int = 0): Boolean {
        stop()

        return try {
            val player = MediaPlayer.create(
                context,
                resourceId,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                0
            ) ?: return false

            player.isLooping = true

            if (gradualDurationSeconds > 0) {
                player.setVolume(0.05f, 0.05f)
                player.start()
                startVolumeRamp(player, gradualDurationSeconds)
            } else {
                player.setVolume(1.0f, 1.0f)
                player.start()
            }

            mediaPlayer = player
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun startVolumeRamp(player: MediaPlayer, durationSeconds: Int) {
        volumeRampJob?.cancel()
        volumeRampJob = scope.launch {
            val totalSteps = (durationSeconds * 2).coerceAtLeast(1)
            val stepDelayMs = 500L
            val volumeDelta = (1.0f - 0.05f) / totalSteps

            var currentVolume = 0.05f
            while (isActive && currentVolume < 1.0f) {
                delay(stepDelayMs)
                currentVolume = (currentVolume + volumeDelta).coerceAtMost(1.0f)
                try {
                    player.setVolume(currentVolume, currentVolume)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    fun stop() {
        volumeRampJob?.cancel()
        volumeRampJob = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }
}
