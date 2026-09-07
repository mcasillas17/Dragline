package com.mcasillas.dragline.ui.alarms

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.usecase.DeleteAlarmUseCase
import com.mcasillas.dragline.domain.usecase.GetAlarmsUseCase
import com.mcasillas.dragline.domain.usecase.ToggleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val alarmScheduler: AlarmScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmsUiState(isLoading = true))
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    init {
        observeAlarms()
        checkPermissions()
    }

    private fun observeAlarms() {
        getAlarmsUseCase()
            .onEach { alarmsList ->
                val now = System.currentTimeMillis()
                val nextActive = alarmsList
                    .filter { it.isEnabled && it.nextTriggerEpochMs > now }
                    .minByOrNull { it.nextTriggerEpochMs }

                val summary = nextActive?.let { formatNextOccurrenceSummary(it.nextTriggerEpochMs) }

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        alarms = alarmsList,
                        nextAlarm = nextActive,
                        nextAlarmSummary = summary
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleAlarm(alarmId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(alarmId, isEnabled)
        }
    }

    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarmId)
        }
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

    private fun formatNextOccurrenceSummary(epochMs: Long): String {
        val triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
        val now = LocalDateTime.now()

        val minutesUntil = ChronoUnit.MINUTES.between(now, triggerTime)
        val hours = minutesUntil / 60
        val remainingMinutes = minutesUntil % 60

        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val formattedTime = triggerTime.format(timeFormatter)

        val countdown = when {
            hours > 0 && remainingMinutes > 0 -> "in ${hours}h ${remainingMinutes}m"
            hours > 0 -> "in ${hours}h"
            remainingMinutes > 0 -> "in ${remainingMinutes}m"
            else -> "in less than a minute"
        }

        return "$formattedTime • $countdown"
    }
}
