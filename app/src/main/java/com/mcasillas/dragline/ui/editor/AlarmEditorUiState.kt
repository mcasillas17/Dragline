package com.mcasillas.dragline.ui.editor

import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.WakeChallengeType

enum class EditorScheduleConcept(val displayName: String, val isAvailable: Boolean) {
    FIXED_TIME("Fixed Time", true),
    SUNRISE("Sunrise", false),
    FIRST_MEETING("First Meeting", false)
}

data class AlarmEditorUiState(
    val alarmId: Long = 0L,
    val label: String = "Morning Alarm",
    val isEnabled: Boolean = true,
    val scheduleConcept: EditorScheduleConcept = EditorScheduleConcept.FIXED_TIME,
    val hour: Int = 7,
    val minute: Int = 0,
    val selectedDays: Set<DayOfWeek> = DayOfWeek.WEEKDAYS,
    val isSpotifySelected: Boolean = false,
    val selectedSpotifyPlaylistId: String = "spotify_morning_indie",
    val selectedLocalSound: String = "beacon",
    val localFallbackSound: String = "beacon",
    val wakeChallengeType: WakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
    val isVibrationEnabled: Boolean = true,
    val isGradualVolumeEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val validationError: String? = null
)
