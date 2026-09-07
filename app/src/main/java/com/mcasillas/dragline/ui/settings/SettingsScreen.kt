package com.mcasillas.dragline.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mcasillas.dragline.domain.model.AppTheme
import com.mcasillas.dragline.domain.model.UserSettings
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.ui.common.TetherDivider
import com.mcasillas.dragline.ui.theme.DraglineTheme
import com.mcasillas.dragline.ui.theme.StatusSuccess
import com.mcasillas.dragline.ui.theme.StatusWarning
import com.mcasillas.dragline.ui.theme.TetherAmber
import com.mcasillas.dragline.ui.theme.TetherCyan

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    SettingsScreenContent(
        uiState = uiState,
        onUpdateTheme = { viewModel.updateTheme(it) },
        onUpdateFallbackSound = { viewModel.updateFallbackSound(it) },
        onUpdateWakeChallenge = { viewModel.updateWakeChallenge(it) },
        onUpdateExclusionDays = { viewModel.updateExclusionDays(it) },
        onUpdateGradualVolume = { viewModel.updateGradualVolume(it) },
        onUpdateVibration = { viewModel.updateVibration(it) },
        onOpenExactAlarmSettings = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        },
        onOpenNotificationSettings = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        },
        onOpenGitHub = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mcasillas17/Dragline"))
            context.startActivity(intent)
        },
        modifier = modifier
    )
}

@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    onUpdateTheme: (AppTheme) -> Unit,
    onUpdateFallbackSound: (String) -> Unit,
    onUpdateWakeChallenge: (WakeChallengeType) -> Unit,
    onUpdateExclusionDays: (Int) -> Unit,
    onUpdateGradualVolume: (Int) -> Unit,
    onUpdateVibration: (Boolean) -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenGitHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preferences, waking friction, and device permissions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Theme Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "APPEARANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTheme.entries.forEach { theme ->
                                FilterChip(
                                    selected = uiState.settings.theme == theme,
                                    onClick = { onUpdateTheme(theme) },
                                    label = { Text(theme.displayName) },
                                    leadingIcon = if (uiState.settings.theme == theme) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // Defaults & Smart Rotation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ALARM DEFAULTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Default Fallback Sound:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("beacon" to "Gentle Beacon", "resonance" to "Resonant Tether").forEach { (id, name) ->
                                FilterChip(
                                    selected = uiState.settings.defaultFallbackSound == id,
                                    onClick = { onUpdateFallbackSound(id) },
                                    label = { Text(name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Recent Song Exclusion Window:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Avoid replaying songs heard in the past N days to prevent alarm habituation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(7 to "7 Days", 14 to "14 Days", 30 to "30 Days").forEach { (days, label) ->
                                FilterChip(
                                    selected = uiState.settings.recentSongExclusionDays == days,
                                    onClick = { onUpdateExclusionDays(days) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Gradual Volume Duration:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Time taken to smoothly increase volume from soft chime to full alarm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0 to "Off", 30 to "30s", 60 to "60s", 120 to "2m").forEach { (seconds, label) ->
                                FilterChip(
                                    selected = uiState.settings.gradualVolumeDurationSeconds == seconds,
                                    onClick = { onUpdateGradualVolume(seconds) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Vibration by Default", style = MaterialTheme.typography.bodyLarge)
                                Text("Pulsed vibration alongside audio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = uiState.settings.isVibrationEnabled,
                                onCheckedChange = onUpdateVibration,
                                colors = SwitchDefaults.colors(checkedThumbColor = TetherAmber)
                            )
                        }
                    }
                }
            }

            item { TetherDivider() }

            // System Permissions Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SYSTEM PERMISSIONS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Exact Alarm
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (uiState.hasExactAlarmPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (uiState.hasExactAlarmPermission) StatusSuccess else StatusWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Exact Alarms", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    if (uiState.hasExactAlarmPermission) "Granted • Accurate to the second" else "Not granted • Alarms may delay during idle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!uiState.hasExactAlarmPermission) {
                                OutlinedButton(
                                    onClick = onOpenExactAlarmSettings,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Fix")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (uiState.hasNotificationPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (uiState.hasNotificationPermission) StatusSuccess else StatusWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    if (uiState.hasNotificationPermission) "Granted • Waking alerts enabled" else "Not granted • Ringing heads-up disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!uiState.hasNotificationPermission) {
                                OutlinedButton(
                                    onClick = onOpenNotificationSettings,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Fix")
                                }
                            }
                        }
                    }
                }
            }

            item { TetherDivider() }

            // About & Open Source
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TetherAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("About Dragline", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dragline is an open-source smart alarm designed to overcome alarm fatigue through music rotation and challenge-based waking without snoozing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Version: 0.1.0 (Developer Preview)", style = MaterialTheme.typography.bodySmall)
                        Text("License: MIT Open Source License", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onOpenGitHub,
                            border = BorderStroke(1.dp, TetherCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TetherCyan),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("View on GitHub")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F12)
@Composable
fun SettingsPreview() {
    DraglineTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(),
            onUpdateTheme = {},
            onUpdateFallbackSound = {},
            onUpdateWakeChallenge = {},
            onUpdateExclusionDays = {},
            onUpdateGradualVolume = {},
            onUpdateVibration = {},
            onOpenExactAlarmSettings = {},
            onOpenNotificationSettings = {},
            onOpenGitHub = {}
        )
    }
}
