package com.mcasillas.dragline.data.repository

import com.mcasillas.dragline.data.local.dao.PlayedTrackDao
import com.mcasillas.dragline.data.local.entity.PlayedTrackEntity
import com.mcasillas.dragline.domain.model.PlayedTrack
import com.mcasillas.dragline.domain.repository.TrackHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackHistoryRepositoryImpl @Inject constructor(
    private val playedTrackDao: PlayedTrackDao
) : TrackHistoryRepository {

    override suspend fun recordTrackPlayed(trackId: String, playedAtEpochMs: Long) {
        playedTrackDao.insert(
            PlayedTrackEntity(
                trackId = trackId,
                playedAtEpochMs = playedAtEpochMs
            )
        )
    }

    override suspend fun getRecentlyPlayedTrackIds(sinceEpochMs: Long): Set<String> {
        return playedTrackDao.getRecentlyPlayed(sinceEpochMs)
            .map { it.trackId }
            .toSet()
    }

    override suspend fun getAllTrackPlayHistory(): List<PlayedTrack> {
        return playedTrackDao.getAllHistory().map {
            PlayedTrack(it.trackId, it.playedAtEpochMs)
        }
    }

    override suspend fun clearHistory() {
        playedTrackDao.clearHistory()
    }
}
