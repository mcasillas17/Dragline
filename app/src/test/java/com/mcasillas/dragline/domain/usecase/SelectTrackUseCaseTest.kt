package com.mcasillas.dragline.domain.usecase

import com.mcasillas.dragline.domain.model.PlayedTrack
import com.mcasillas.dragline.domain.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SelectTrackUseCaseTest {

    private lateinit var selectTrackUseCase: SelectTrackUseCase

    private val track1 = Track("t1", "Track One", "Artist A")
    private val track2 = Track("t2", "Track Two", "Artist B")
    private val track3 = Track("t3", "Track Three", "Artist C")
    private val allTracks = listOf(track1, track2, track3)

    @Before
    fun setup() {
        selectTrackUseCase = SelectTrackUseCase()
    }

    @Test
    fun `returns null when available tracks list is empty`() {
        val result = selectTrackUseCase.execute(
            availableTracks = emptyList(),
            recentlyPlayedTrackIds = emptySet()
        )
        assertNull(result)
    }

    @Test
    fun `excludes recently played tracks when unplayed alternatives exist`() {
        // Track 1 and Track 2 were played recently
        val recentlyPlayed = setOf("t1", "t2")

        val result = selectTrackUseCase.execute(
            availableTracks = allTracks,
            recentlyPlayedTrackIds = recentlyPlayed
        )

        assertNotNull(result)
        assertEquals("t3", result?.id)
    }

    @Test
    fun `deterministic selection with seed returns consistent track`() {
        val recentlyPlayed = setOf("t1") // t2 and t3 are candidates

        val result1 = selectTrackUseCase.execute(
            availableTracks = allTracks,
            recentlyPlayedTrackIds = recentlyPlayed,
            seed = 42L
        )

        val result2 = selectTrackUseCase.execute(
            availableTracks = allTracks,
            recentlyPlayedTrackIds = recentlyPlayed,
            seed = 42L
        )

        assertNotNull(result1)
        assertEquals(result1?.id, result2?.id)
        assertTrue(result1?.id in setOf("t2", "t3"))
    }

    @Test
    fun `falls back to least recently played track when all tracks have been played`() {
        // All 3 tracks are in recently played
        val recentlyPlayed = setOf("t1", "t2", "t3")

        // Play history: t1 played at 1000, t2 played at 3000, t3 played at 2000
        val history = listOf(
            PlayedTrack("t1", playedAtEpochMs = 1000L),
            PlayedTrack("t2", playedAtEpochMs = 3000L),
            PlayedTrack("t3", playedAtEpochMs = 2000L)
        )

        val result = selectTrackUseCase.execute(
            availableTracks = allTracks,
            recentlyPlayedTrackIds = recentlyPlayed,
            playHistory = history
        )

        // t1 was played longest ago (1000ms), so it should be chosen
        assertNotNull(result)
        assertEquals("t1", result?.id)
    }
}
