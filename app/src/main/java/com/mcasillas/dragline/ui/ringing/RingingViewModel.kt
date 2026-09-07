package com.mcasillas.dragline.ui.ringing

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcasillas.dragline.alarm.service.AlarmRingingService
import com.mcasillas.dragline.audio.VibratorHelper
import com.mcasillas.dragline.challenge.impl.HoldToWakeChallenge
import com.mcasillas.dragline.domain.model.WakeChallengeType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RingingUiState(
    val currentTime: String = "",
    val alarmId: Long = 0L,
    val alarmLabel: String = "Alarm",
    val soundSourceDisplayName: String = "Gentle Beacon",
    val isFallbackActive: Boolean = false,
    val wakeChallengeType: WakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
    val holdProgress: Float = 0f,
    val isHolding: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class RingingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vibratorHelper: VibratorHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(RingingUiState())
    val uiState: StateFlow<RingingUiState> = _uiState.asStateFlow()

    private val holdChallenge = HoldToWakeChallenge(requiredDurationMs = 3000L)
    private var holdJob: Job? = null

    init {
        startClockUpdates()
        observeServiceRingingState()
    }

    private fun startClockUpdates() {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("h:mm:ss a")
            while (isActive) {
                _uiState.update { it.copy(currentTime = LocalTime.now().format(formatter)) }
                delay(1000L)
            }
        }
    }

    private fun observeServiceRingingState() {
        AlarmRingingService.ringingState
            .onEach { ringingInfo ->
                if (ringingInfo != null) {
                    _uiState.update {
                        it.copy(
                            alarmId = ringingInfo.alarmId,
                            alarmLabel = ringingInfo.label,
                            soundSourceDisplayName = ringingInfo.soundSourceDisplayName,
                            isFallbackActive = ringingInfo.isFallbackActive,
                            wakeChallengeType = ringingInfo.wakeChallengeType
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onHoldStarted() {
        if (_uiState.value.isCompleted) return

        _uiState.update { it.copy(isHolding = true) }
        holdJob?.cancel()

        holdJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = holdChallenge.calculateProgress(elapsed)
                _uiState.update { it.copy(holdProgress = progress) }

                if (holdChallenge.isComplete(elapsed)) {
                    // Challenge completed!
                    vibratorHelper.vibrateShortTick()
                    _uiState.update { it.copy(isCompleted = true, holdProgress = 1f, isHolding = false) }
                    completeAlarm()
                    break
                }
                delay(16L) // ~60fps smooth progress
            }
        }
    }

    fun onHoldReleased() {
        if (_uiState.value.isCompleted) return

        holdJob?.cancel()
        holdJob = null
        _uiState.update { it.copy(isHolding = false, holdProgress = 0f) }
    }

    fun debugDismiss() {
        completeAlarm()
    }

    private fun completeAlarm() {
        val dismissIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_DISMISS_ALARM
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, _uiState.value.alarmId)
        }
        context.startService(dismissIntent)
    }
}
