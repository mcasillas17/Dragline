package com.mcasillas.dragline.ui.alarms

import com.mcasillas.dragline.domain.model.Alarm

data class AlarmsUiState(
    val isLoading: Boolean = false,
    val alarms: List<Alarm> = emptyList(),
    val nextAlarm: Alarm? = null,
    val nextAlarmSummary: String? = null,
    val hasExactAlarmPermission: Boolean = true,
    val hasNotificationPermission: Boolean = true,
    val userMessage: String? = null
)
