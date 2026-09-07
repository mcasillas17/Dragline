package com.mcasillas.dragline.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val previewUrl: String? = null
)

data class PlayedTrack(
    val trackId: String,
    val playedAtEpochMs: Long
)
