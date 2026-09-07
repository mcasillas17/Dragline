package com.mcasillas.dragline.audio

import com.mcasillas.dragline.domain.model.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSpotifyProvider @Inject constructor() : SoundProvider {

    override val id: String = "spotify"
    override val displayName: String = "Spotify (Preview Mode)"
    override val connectionState: ProviderConnectionState = ProviderConnectionState.MOCK_MODE

    private val playlists = listOf(
        SoundCollection(
            id = "spotify_morning_indie",
            title = "Morning Indie Acoustic",
            description = "Gentle acoustic morning songs for a warm wake-up.",
            trackCount = 5,
            providerId = id
        ),
        SoundCollection(
            id = "spotify_electronic_pulse",
            title = "Electric Sunrise",
            description = "High-energy synth and electro rhythms to wake you up immediately.",
            trackCount = 4,
            providerId = id
        ),
        SoundCollection(
            id = "spotify_deep_focus",
            title = "Ambient Horizon",
            description = "Atmospheric instrumental soundscapes with gradual build-up.",
            trackCount = 4,
            providerId = id
        )
    )

    private val tracksByCollection = mapOf(
        "spotify_morning_indie" to listOf(
            Track("sp_1", "Bloom", "The Paper Kites", "Woodland", 210000L),
            Track("sp_2", "Holocene", "Bon Iver", "Bon Iver", 336000L),
            Track("sp_3", "First Day of My Life", "Bright Eyes", "I'm Wide Awake", 188000L),
            Track("sp_4", "Georgia", "Vance Joy", "Dream Your Life Away", 231000L),
            Track("sp_5", "Rivers and Roads", "The Head and the Heart", "Self-Titled", 284000L)
        ),
        "spotify_electronic_pulse" to listOf(
            Track("sp_6", "Midnight City", "M83", "Hurry Up, We're Dreaming", 243000L),
            Track("sp_7", "Genesis", "Justice", "Cross", 234000L),
            Track("sp_8", "Resonance", "HOME", "Odyssey", 212000L),
            Track("sp_9", "Strobe", "deadmau5", "For Lack of a Better Name", 637000L)
        ),
        "spotify_deep_focus" to listOf(
            Track("sp_10", "An Ending (Ascent)", "Brian Eno", "Apollo", 264000L),
            Track("sp_11", "Day One", "Hans Zimmer", "Interstellar OST", 199000L),
            Track("sp_12", "Avril 14th", "Aphex Twin", "Drukqs", 125000L),
            Track("sp_13", "Experience", "Ludovico Einaudi", "In a Time Lapse", 315000L)
        )
    )

    override suspend fun authenticate(): Result<Unit> {
        // Mock authentication returns success for preview purposes
        return Result.success(Unit)
    }

    override suspend fun listCollections(): List<SoundCollection> {
        return playlists
    }

    override suspend fun getTracks(collectionId: String): List<Track> {
        return tracksByCollection[collectionId] ?: emptyList()
    }

    override suspend fun playTrack(track: Track): PlaybackResult {
        // Spotify streaming playback is not implemented in this skeleton.
        // It cleanly reports failure with canFallback = true so the alarm falls back to local audio.
        return PlaybackResult.Failure(
            reason = "Spotify playback requires active SDK connection. Falling back to local offline sound.",
            canFallback = true
        )
    }

    override suspend fun stopPlayback() {
        // No-op for mock provider
    }
}
