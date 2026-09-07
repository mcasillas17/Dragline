package com.mcasillas.dragline.domain.model

sealed interface SoundSource {
    val id: String
    val displayName: String

    data class LocalSound(
        override val id: String = "beacon",
        override val displayName: String = "Gentle Beacon",
        val resourceName: String = "beacon"
    ) : SoundSource

    data class SpotifyPlaylist(
        override val id: String,
        override val displayName: String,
        val uri: String,
        val trackCount: Int = 0,
        val isMock: Boolean = true
    ) : SoundSource
}
