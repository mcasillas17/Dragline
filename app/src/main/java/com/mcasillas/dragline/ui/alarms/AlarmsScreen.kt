package com.mcasillas.dragline.ui.alarms

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mcasillas.dragline.R
import com.mcasillas.dragline.domain.model.Alarm
import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.Schedule
import com.mcasillas.dragline.domain.model.SoundSource
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.ui.alarms.components.AlarmItemCard
import com.mcasillas.dragline.ui.alarms.components.EmptyAlarmsView
import com.mcasillas.dragline.ui.alarms.components.NextAlarmCard
import com.mcasillas.dragline.ui.common.PermissionRationaleCard
import com.mcasillas.dragline.ui.theme.DraglineTheme
import com.mcasillas.dragline.ui.theme.TetherAmber
import java.time.LocalTime

@Composable
fun AlarmsScreen(
    viewModel: AlarmsViewModel,
    onNavigateToEditor: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    AlarmsScreenContent(
        uiState = uiState,
        onToggleAlarm = { id, enabled -> viewModel.toggleAlarm(id, enabled) },
        onEditAlarm = { id -> onNavigateToEditor(id) },
        onDeleteAlarm = { id -> viewModel.deleteAlarm(id) },
        onCreateAlarm = { onNavigateToEditor(null) },
        onRequestExactAlarmPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        },
        onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        },
        modifier = modifier
    )
}

@Composable
fun AlarmsScreenContent(
    uiState: AlarmsUiState,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onEditAlarm: (Long) -> Unit,
    onDeleteAlarm: (Long) -> Unit,
    onCreateAlarm: () -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateAlarm,
                containerColor = TetherAmber,
                contentColor = androidx.compose.ui.graphics.Color.Black,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Alarm")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TetherAmber)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dragline_tether),
                            contentDescription = null,
                            tint = TetherAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Dragline",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Exact Alarm permission card if missing
                if (!uiState.hasExactAlarmPermission) {
                    item {
                        PermissionRationaleCard(
                            title = "Exact Alarms Required",
                            description = "To ensure Dragline rings reliably at the exact second even during deep battery sleep, grant exact alarm scheduling in system settings.",
                            buttonText = "Grant Permission",
                            icon = Icons.Default.Alarm,
                            onActionClick = onRequestExactAlarmPermission
                        )
                    }
                }

                // Notification permission card if missing
                if (!uiState.hasNotificationPermission) {
                    item {
                        PermissionRationaleCard(
                            title = "Notifications Required",
                            description = "Android requires notification permission to launch the waking full-screen experience and alarm ringing screen.",
                            buttonText = "Grant Permission",
                            icon = Icons.Default.Notifications,
                            onActionClick = onRequestNotificationPermission
                        )
                    }
                }

                // Prominent Next Alarm Hero
                item {
                    NextAlarmCard(
                        alarm = uiState.nextAlarm,
                        summary = uiState.nextAlarmSummary
                    )
                }

                // Configured Alarms List or Empty State
                if (uiState.alarms.isEmpty()) {
                    item {
                        EmptyAlarmsView(onCreateAlarmClick = onCreateAlarm)
                    }
                } else {
                    item {
                        Text(
                            text = "CONFIGURED ALARMS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(uiState.alarms, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            onToggle = { enabled -> onToggleAlarm(alarm.id, enabled) },
                            onClick = { onEditAlarm(alarm.id) },
                            onDelete = { onDeleteAlarm(alarm.id) }
                        )
                    }
                }

                // Bottom spacer for FAB clearance
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F12)
@Composable
fun AlarmsScreenPreview() {
    val sampleAlarms = listOf(
        Alarm(
            id = 1L,
            label = "Weekday Rise",
            isEnabled = true,
            schedule = Schedule.FixedTime(LocalTime.of(7, 0), DayOfWeek.WEEKDAYS),
            soundSource = SoundSource.SpotifyPlaylist("sp_1", "Morning Indie Acoustic", "spotify:playlist:1"),
            wakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
            nextTriggerEpochMs = System.currentTimeMillis() + 3600000 * 7
        ),
        Alarm(
            id = 2L,
            label = "Weekend Slow Morning",
            isEnabled = false,
            schedule = Schedule.FixedTime(LocalTime.of(9, 30), DayOfWeek.WEEKENDS),
            soundSource = SoundSource.LocalSound("beacon", "Gentle Beacon"),
            wakeChallengeType = WakeChallengeType.HOLD_TO_WAKE
        )
    )

    DraglineTheme {
        AlarmsScreenContent(
            uiState = AlarmsUiState(
                isLoading = false,
                alarms = sampleAlarms,
                nextAlarm = sampleAlarms.first(),
                nextAlarmSummary = "7:00 AM • in 7h 0m",
                hasExactAlarmPermission = true,
                hasNotificationPermission = true
            ),
            onToggleAlarm = { _, _ -> },
            onEditAlarm = {},
            onDeleteAlarm = {},
            onCreateAlarm = {},
            onRequestExactAlarmPermission = {},
            onRequestNotificationPermission = {}
        )
    }
}
