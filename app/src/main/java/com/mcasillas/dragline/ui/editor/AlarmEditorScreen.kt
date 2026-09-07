package com.mcasillas.dragline.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.ui.common.TetherDivider
import com.mcasillas.dragline.ui.theme.DraglineTheme
import com.mcasillas.dragline.ui.theme.StatusMock
import com.mcasillas.dragline.ui.theme.TetherAmber
import com.mcasillas.dragline.ui.theme.TetherCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorScreen(
    viewModel: AlarmEditorViewModel,
    alarmId: Long?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (alarmId == null || alarmId == 0L) "New Alarm" else "Edit Alarm",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel and go back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveAlarm(onSuccess = onNavigateBack) },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TetherAmber,
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Validation Error if any
            if (uiState.validationError != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.validationError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Time Selector Card
            item {
                TimePickerCard(
                    hour = uiState.hour,
                    minute = uiState.minute,
                    onTimeChanged = { h, m -> viewModel.updateTime(h, m) }
                )
            }

            // Schedule Concept (Fixed, Sunrise, Meeting)
            item {
                ScheduleConceptSelector(
                    selected = uiState.scheduleConcept,
                    onSelect = { viewModel.setScheduleConcept(it) }
                )
            }

            // Repeat Weekdays
            item {
                RepeatDaysSelector(
                    selectedDays = uiState.selectedDays,
                    onDayToggled = { viewModel.toggleDay(it) }
                )
            }

            item { TetherDivider() }

            // Label Input
            item {
                OutlinedTextField(
                    value = uiState.label,
                    onValueChange = { viewModel.updateLabel(it) },
                    label = { Text("Alarm Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TetherAmber,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Sound Source
            item {
                SoundSourceSection(
                    isSpotifySelected = uiState.isSpotifySelected,
                    selectedPlaylistId = uiState.selectedSpotifyPlaylistId,
                    selectedLocalSound = uiState.selectedLocalSound,
                    onToggleSource = { viewModel.toggleSoundSource(it) },
                    onSelectPlaylist = { viewModel.selectSpotifyPlaylist(it) },
                    onSelectLocalSound = { viewModel.selectLocalSound(it) }
                )
            }

            // Wake Challenge
            item {
                WakeChallengeSection(
                    selectedChallenge = uiState.wakeChallengeType,
                    onSelectChallenge = { viewModel.selectWakeChallenge(it) }
                )
            }

            // Audio & Experience Toggles
            item {
                TogglesSection(
                    isVibrationEnabled = uiState.isVibrationEnabled,
                    isGradualVolumeEnabled = uiState.isGradualVolumeEnabled,
                    selectedFallbackSound = uiState.localFallbackSound,
                    onToggleVibration = { viewModel.toggleVibration(it) },
                    onToggleGradualVolume = { viewModel.toggleGradualVolume(it) },
                    onSelectFallbackSound = { viewModel.selectFallbackSound(it) }
                )
            }

            // Delete Button (if editing existing alarm)
            if (uiState.alarmId != 0L) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.deleteAlarm(onSuccess = onNavigateBack) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Alarm")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TimePickerCard(
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit
) {
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val isPm = hour >= 12

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ALARM TIME",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hour controls
                NumberSpinner(
                    value = displayHour,
                    onIncrement = {
                        val nextH = if (isPm) {
                            if (displayHour == 12) 13 else if (displayHour == 11) 12 else hour + 1
                        } else {
                            if (displayHour == 12) 1 else (hour + 1) % 12
                        }
                        onTimeChanged(nextH, minute)
                    },
                    onDecrement = {
                        val prevH = if (isPm) {
                            if (displayHour == 1) 23 else hour - 1
                        } else {
                            if (displayHour == 1) 0 else if (displayHour == 12) 11 else hour - 1
                        }
                        onTimeChanged(prevH, minute)
                    }
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Minute controls
                NumberSpinner(
                    value = minute,
                    formatString = "%02d",
                    onIncrement = { onTimeChanged(hour, (minute + 5) % 60) },
                    onDecrement = { onTimeChanged(hour, (minute - 5 + 60) % 60) }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // AM / PM Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (!isPm) TetherAmber else MaterialTheme.colorScheme.surface)
                            .clickable {
                                if (isPm) onTimeChanged(hour - 12, minute)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!isPm) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (isPm) TetherAmber else MaterialTheme.colorScheme.surface)
                            .clickable {
                                if (!isPm) onTimeChanged(hour + 12, minute)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isPm) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberSpinner(
    value: Int,
    formatString: String = "%d",
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = TetherCyan)
        }
        Text(
            text = String.format(formatString, value),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = TetherCyan)
        }
    }
}

@Composable
fun ScheduleConceptSelector(
    selected: EditorScheduleConcept,
    onSelect: (EditorScheduleConcept) -> Unit
) {
    Column {
        Text(
            text = "SCHEDULE TYPE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditorScheduleConcept.entries.forEach { concept ->
                FilterChip(
                    selected = selected == concept,
                    onClick = { onSelect(concept) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(concept.displayName)
                            if (!concept.isAvailable) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Soon",
                                    fontSize = 9.sp,
                                    color = TetherAmber,
                                    modifier = Modifier
                                        .background(TetherAmber.copy(alpha = 0.2f), CircleShape)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TetherAmber.copy(alpha = 0.2f),
                        selectedLabelColor = TetherAmber
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected == concept,
                        selectedBorderColor = TetherAmber
                    )
                )
            }
        }
    }
}

@Composable
fun RepeatDaysSelector(
    selectedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek) -> Unit
) {
    Column {
        Text(
            text = "REPEAT DAYS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.entries.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) TetherCyan else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onDayToggled(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.shortName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SoundSourceSection(
    isSpotifySelected: Boolean,
    selectedPlaylistId: String,
    selectedLocalSound: String,
    onToggleSource: (Boolean) -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onSelectLocalSound: (String) -> Unit
) {
    Column {
        Text(
            text = "SOUND SOURCE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !isSpotifySelected,
                onClick = { onToggleSource(false) },
                label = { Text("Local Bundled Sounds") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TetherAmber.copy(alpha = 0.2f),
                    selectedLabelColor = TetherAmber
                )
            )
            FilterChip(
                selected = isSpotifySelected,
                onClick = { onToggleSource(true) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Spotify")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Preview",
                            fontSize = 9.sp,
                            color = StatusMock,
                            modifier = Modifier
                                .background(StatusMock.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusMock.copy(alpha = 0.2f),
                    selectedLabelColor = StatusMock
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isSpotifySelected) {
            val playlists = listOf(
                "spotify_morning_indie" to "Morning Indie Acoustic",
                "spotify_electronic_pulse" to "Electric Sunrise",
                "spotify_deep_focus" to "Ambient Horizon"
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                playlists.forEach { (id, name) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPlaylist(id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPlaylistId == id) {
                                StatusMock.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedPlaylistId == id) StatusMock else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = StatusMock)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            val localSounds = listOf(
                "beacon" to "Gentle Beacon",
                "resonance" to "Resonant Tether"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                localSounds.forEach { (id, name) ->
                    FilterChip(
                        selected = selectedLocalSound == id,
                        onClick = { onSelectLocalSound(id) },
                        label = { Text(name) }
                    )
                }
            }
        }
    }
}

@Composable
fun WakeChallengeSection(
    selectedChallenge: WakeChallengeType,
    onSelectChallenge: (WakeChallengeType) -> Unit
) {
    Column {
        Text(
            text = "WAKE-UP TASK (NO SNOOZING)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WakeChallengeType.entries.forEach { challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = challenge.isImplemented) {
                            onSelectChallenge(challenge)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedChallenge == challenge) {
                            TetherCyan.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedChallenge == challenge) TetherCyan else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(challenge.title, style = MaterialTheme.typography.titleMedium)
                                if (!challenge.isImplemented) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Coming Soon",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = challenge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (selectedChallenge == challenge) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TetherCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TogglesSection(
    isVibrationEnabled: Boolean,
    isGradualVolumeEnabled: Boolean,
    selectedFallbackSound: String,
    onToggleVibration: (Boolean) -> Unit,
    onToggleGradualVolume: (Boolean) -> Unit,
    onSelectFallbackSound: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RELIABILITY & AUDIO SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Vibration", style = MaterialTheme.typography.titleMedium)
                    Text("Pulsed wake vibration pattern", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = onToggleVibration,
                    colors = SwitchDefaults.colors(checkedThumbColor = TetherAmber)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gradual Volume Increase", style = MaterialTheme.typography.titleMedium)
                    Text("Gently ramp volume from whisper to full", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isGradualVolumeEnabled,
                    onCheckedChange = onToggleGradualVolume,
                    colors = SwitchDefaults.colors(checkedThumbColor = TetherAmber)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Guaranteed Local Fallback Sound:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("beacon" to "Gentle Beacon", "resonance" to "Resonant Tether").forEach { (id, name) ->
                    FilterChip(
                        selected = selectedFallbackSound == id,
                        onClick = { onSelectFallbackSound(id) },
                        label = { Text(name) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F12)
@Composable
fun AlarmEditorPreview() {
    DraglineTheme {
        AlarmEditorScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            alarmId = null,
            onNavigateBack = {}
        )
    }
}
