package com.mcasillas.dragline.audio

import android.content.Context
import com.mcasillas.dragline.R
import com.mcasillas.dragline.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSoundProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlayer: AudioPlayer
) : SoundProvider {

    override val id: String = "local"
    override val displayName: String = "Bundled Sounds"
    override val connectionState: ProviderConnectionState = ProviderConnectionState.CONNECTED

    private val localTracks = listOf(
        Track(
            id = "beacon",
            title = "Gentle Beacon",
            artist = "Dragline Audio",
            album = "Offline Essentials",
            durationMs = 4000L
        ),
        Track(
            id = "resonance",
            title = "Resonant Tether",
            artist = "Dragline Audio",
            album = "Offline Essentials",
            durationMs = 4000L
        )
    )

    override suspend fun authenticate(): Result<Unit> = Result.success(Unit)

    override suspend fun listCollections(): List<SoundCollection> {
        return listOf(
            SoundCollection(
                id = "local_builtins",
                title = "Offline Alarm Tones",
                description = "High-reliability bundled local audio that works without network or subscriptions.",
                trackCount = localTracks.size,
                providerId = id
            )
        )
    }

    override suspend fun getTracks(collectionId: String): List<Track> {
        return localTracks
    }

    override suspend fun playTrack(track: Track): PlaybackResult {
        val resourceId = when (track.id) {
            "resonance" -> R.raw.resonance
            else -> R.raw.beacon
        }
        val success = audioPlayer.playRaw(resourceId)
        return if (success) {
            PlaybackResult.Success(track)
        } else {
            PlaybackResult.Failure("Failed to initialize MediaPlayer for local track ${track.id}")
        }
    }

    override suspend fun stopPlayback() {
        audioPlayer.stop()
    }

    fun getLocalTrackResource(trackId: String): Int {
        return when (trackId) {
            "resonance" -> R.raw.resonance
            else -> R.raw.beacon
        }
    }
}
