package com.mcasillas.dragline.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mcasillas.dragline.data.local.entity.PlayedTrackEntity

@Dao
interface PlayedTrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playedTrack: PlayedTrackEntity)

    @Query("SELECT * FROM played_tracks WHERE playedAtEpochMs >= :sinceEpochMs ORDER BY playedAtEpochMs DESC")
    suspend fun getRecentlyPlayed(sinceEpochMs: Long): List<PlayedTrackEntity>

    @Query("SELECT * FROM played_tracks ORDER BY playedAtEpochMs DESC")
    suspend fun getAllHistory(): List<PlayedTrackEntity>

    @Query("DELETE FROM played_tracks")
    suspend fun clearHistory()
}
