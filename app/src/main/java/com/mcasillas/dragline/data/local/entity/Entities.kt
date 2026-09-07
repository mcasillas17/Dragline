package com.mcasillas.dragline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val label: String,
    val isEnabled: Boolean,
    val scheduleType: String, // FIXED_TIME, SUNRISE, FIRST_MEETING
    val hour: Int,
    val minute: Int,
    val offsetMinutes: Int,
    val repeatDays: String, // Comma-separated enum names, e.g. "MONDAY,TUESDAY"
    val soundSourceType: String, // LOCAL, SPOTIFY
    val soundSourceId: String,
    val soundSourceDisplayName: String,
    val soundSourceUri: String,
    val localFallbackSound: String,
    val wakeChallengeType: String,
    val isVibrationEnabled: Boolean,
    val isGradualVolumeEnabled: Boolean,
    val nextTriggerEpochMs: Long
)

@Entity(tableName = "played_tracks")
data class PlayedTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val trackId: String,
    val playedAtEpochMs: Long
)
