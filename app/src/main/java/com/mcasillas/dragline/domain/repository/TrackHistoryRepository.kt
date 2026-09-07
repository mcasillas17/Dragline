package com.mcasillas.dragline.domain.repository

import com.mcasillas.dragline.domain.model.PlayedTrack

interface TrackHistoryRepository {
    suspend fun recordTrackPlayed(trackId: String, playedAtEpochMs: Long = System.currentTimeMillis())
    suspend fun getRecentlyPlayedTrackIds(sinceEpochMs: Long): Set<String>
    suspend fun getAllTrackPlayHistory(): List<PlayedTrack>
    suspend fun clearHistory()
}
