package com.mcasillas.dragline.ui.settings

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.domain.model.AppTheme
import com.mcasillas.dragline.domain.model.UserSettings
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val hasExactAlarmPermission: Boolean = true,
    val hasNotificationPermission: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: AlarmScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        checkPermissions()
    }

    private fun observeSettings() {
        settingsRepository.getSettings()
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    fun checkPermissions() {
        val exactAllowed = alarmScheduler.canScheduleExactAlarms()
        val notificationsAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }

        _uiState.update {
            it.copy(
                hasExactAlarmPermission = exactAllowed,
                hasNotificationPermission = notificationsAllowed
            )
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.updateTheme(theme) }
    }

    fun updateFallbackSound(sound: String) {
        viewModelScope.launch { settingsRepository.updateDefaultFallbackSound(sound) }
    }

    fun updateWakeChallenge(challenge: WakeChallengeType) {
        viewModelScope.launch { settingsRepository.updateDefaultWakeChallenge(challenge) }
    }

    fun updateExclusionDays(days: Int) {
        viewModelScope.launch { settingsRepository.updateRecentSongExclusionDays(days) }
    }

    fun updateGradualVolume(seconds: Int) {
        viewModelScope.launch { settingsRepository.updateGradualVolumeDuration(seconds) }
    }

    fun updateVibration(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateVibrationEnabled(enabled) }
    }
}
