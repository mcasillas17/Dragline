package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.domain.model.PlayedTrack
import com.mcasillas.dragline.domain.model.Track
import java.util.Random
import javax.inject.Inject

class SelectTrackUseCase @Inject constructor() {

    /**
     * Deterministically selects a track from [availableTracks] while avoiding tracks
     * present in [recentlyPlayedTrackIds].
     *
     * - If unplayed tracks exist, one is selected from the unplayed pool.
     * - If all tracks have been recently played, it falls back to the least recently played track
     *   based on [playHistory].
     * - If [availableTracks] is empty, returns null.
     */
    fun execute(
        availableTracks: List<Track>,
        recentlyPlayedTrackIds: Set<String>,
        playHistory: List<PlayedTrack> = emptyList(),
        seed: Long? = null
    ): Track? {
        if (availableTracks.isEmpty()) return null

        val unplayedTracks = availableTracks.filterNot { it.id in recentlyPlayedTrackIds }

        if (unplayedTracks.isNotEmpty()) {
            val index = if (seed != null) {
                Random(seed).nextInt(unplayedTracks.size)
            } else {
                (0 until unplayedTracks.size).random()
            }
            return unplayedTracks[index]
        }

        // All tracks have been recently played: find the least recently played track
        val lastPlayedMap = playHistory
            .groupBy { it.trackId }
            .mapValues { (_, entries) -> entries.maxOfOrNull { it.playedAtEpochMs } ?: 0L }

        return availableTracks.minByOrNull { track ->
            lastPlayedMap[track.id] ?: 0L
        } ?: availableTracks.first()
    }
}
