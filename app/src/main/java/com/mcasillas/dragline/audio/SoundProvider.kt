package com.mcasillas.dragline.audio

import com.mcasillas.dragline.domain.model.Track

data class SoundCollection(
    val id: String,
    val title: String,
    val description: String,
    val trackCount: Int,
    val providerId: String
)

sealed interface PlaybackResult {
    data class Success(val track: Track) : PlaybackResult
    data class Failure(val reason: String, val canFallback: Boolean = true) : PlaybackResult
}

enum class ProviderConnectionState {
    CONNECTED,
    DISCONNECTED,
    MOCK_MODE
}

interface SoundProvider {
    val id: String
    val displayName: String
    val connectionState: ProviderConnectionState

    suspend fun authenticate(): Result<Unit>
    suspend fun listCollections(): List<SoundCollection>
    suspend fun getTracks(collectionId: String): List<Track>
    suspend fun playTrack(track: Track): PlaybackResult
    suspend fun stopPlayback()
}
