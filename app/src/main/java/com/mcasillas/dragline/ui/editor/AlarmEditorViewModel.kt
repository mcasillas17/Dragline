package com.mcasillas.dragline.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.Schedule
import com.mcasillas.dragline.domain.model.SoundSource
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.domain.repository.AlarmRepository
import com.mcasillas.dragline.domain.repository.SettingsRepository
import com.mcasillas.dragline.domain.usecase.DeleteAlarmUseCase
import com.mcasillas.dragline.domain.usecase.SaveAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmEditorViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val saveAlarmUseCase: SaveAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmEditorUiState())
    val uiState: StateFlow<AlarmEditorUiState> = _uiState.asStateFlow()

    fun loadAlarm(alarmId: Long?) {
        if (alarmId == null || alarmId == 0L) {
            // New alarm: load default settings
            viewModelScope.launch {
                val settings = settingsRepository.getSettings().first()
                _uiState.update {
                    it.copy(
                        alarmId = 0L,
                        localFallbackSound = settings.defaultFallbackSound,
                        wakeChallengeType = settings.defaultWakeChallenge
                    )
                }
            }
            return
        }

        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch
            val fixedTime = (alarm.schedule as? Schedule.FixedTime)?.time ?: LocalTime.of(7, 0)
            val concept = when (alarm.schedule) {
                is Schedule.FixedTime -> EditorScheduleConcept.FIXED_TIME
                is Schedule.Sunrise -> EditorScheduleConcept.SUNRISE
                is Schedule.FirstMeeting -> EditorScheduleConcept.FIRST_MEETING
            }

            val isSpotify = alarm.soundSource is SoundSource.SpotifyPlaylist
            val playlistId = (alarm.soundSource as? SoundSource.SpotifyPlaylist)?.id ?: "spotify_morning_indie"
            val localSoundId = (alarm.soundSource as? SoundSource.LocalSound)?.id ?: "beacon"

            _uiState.update {
                it.copy(
                    alarmId = alarm.id,
                    label = alarm.label,
                    isEnabled = alarm.isEnabled,
                    scheduleConcept = concept,
                    hour = fixedTime.hour,
                    minute = fixedTime.minute,
                    selectedDays = alarm.schedule.repeatDays,
                    isSpotifySelected = isSpotify,
                    selectedSpotifyPlaylistId = playlistId,
                    selectedLocalSound = localSoundId,
                    localFallbackSound = alarm.localFallbackSound,
                    wakeChallengeType = alarm.wakeChallengeType,
                    isVibrationEnabled = alarm.isVibrationEnabled,
                    isGradualVolumeEnabled = alarm.isGradualVolumeEnabled
                )
            }
        }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour.coerceIn(0, 23), minute = minute.coerceIn(0, 59)) }
    }

    fun toggleDay(day: DayOfWeek) {
        _uiState.update { state ->
            val updatedDays = if (state.selectedDays.contains(day)) {
                state.selectedDays - day
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = updatedDays)
        }
    }

    fun setScheduleConcept(concept: EditorScheduleConcept) {
        _uiState.update { it.copy(scheduleConcept = concept) }
    }

    fun toggleSoundSource(isSpotify: Boolean) {
        _uiState.update { it.copy(isSpotifySelected = isSpotify) }
    }

    fun selectSpotifyPlaylist(playlistId: String) {
        _uiState.update { it.copy(selectedSpotifyPlaylistId = playlistId) }
    }

    fun selectLocalSound(soundId: String) {
        _uiState.update { it.copy(selectedLocalSound = soundId) }
    }

    fun selectFallbackSound(soundId: String) {
        _uiState.update { it.copy(localFallbackSound = soundId) }
    }

    fun selectWakeChallenge(challenge: WakeChallengeType) {
        _uiState.update { it.copy(wakeChallengeType = challenge) }
    }

    fun toggleVibration(enabled: Boolean) {
        _uiState.update { it.copy(isVibrationEnabled = enabled) }
    }

    fun toggleGradualVolume(enabled: Boolean) {
        _uiState.update { it.copy(isGradualVolumeEnabled = enabled) }
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (!state.scheduleConcept.isAvailable) {
            _uiState.update {
                it.copy(validationError = "${state.scheduleConcept.displayName} scheduling is coming soon. Please select Fixed Time for this version.")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, validationError = null) }

        viewModelScope.launch {
            val schedule = Schedule.FixedTime(
                time = LocalTime.of(state.hour, state.minute),
                repeatDays = state.selectedDays
            )

            val soundSource = if (state.isSpotifySelected) {
                val displayName = when (state.selectedSpotifyPlaylistId) {
                    "spotify_electronic_pulse" -> "Electric Sunrise"
                    "spotify_deep_focus" -> "Ambient Horizon"
                    else -> "Morning Indie Acoustic"
                }
                SoundSource.SpotifyPlaylist(
                    id = state.selectedSpotifyPlaylistId,
                    displayName = displayName,
                    uri = "spotify:playlist:${state.selectedSpotifyPlaylistId}",
                    isMock = true
                )
            } else {
                val displayName = if (state.selectedLocalSound == "resonance") "Resonant Tether" else "Gentle Beacon"
                SoundSource.LocalSound(
                    id = state.selectedLocalSound,
                    displayName = displayName
                )
            }

            val alarm = Alarm(
                id = state.alarmId,
                label = state.label.ifBlank { "Alarm" },
                isEnabled = state.isEnabled,
                schedule = schedule,
                soundSource = soundSource,
                localFallbackSound = state.localFallbackSound,
                wakeChallengeType = state.wakeChallengeType,
                isVibrationEnabled = state.isVibrationEnabled,
                isGradualVolumeEnabled = state.isGradualVolumeEnabled
            )

            saveAlarmUseCase(alarm)
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
            onSuccess()
        }
    }

    fun deleteAlarm(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.alarmId != 0L) {
            viewModelScope.launch {
                deleteAlarmUseCase(state.alarmId)
                onSuccess()
            }
        } else {
            onSuccess()
        }
    }
}
