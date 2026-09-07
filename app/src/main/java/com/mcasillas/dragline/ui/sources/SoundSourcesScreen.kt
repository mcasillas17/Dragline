package com.mcasillas.dragline.ui.sources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcasillas.dragline.audio.ProviderConnectionState
import com.mcasillas.dragline.audio.SoundCollection
import com.mcasillas.dragline.domain.model.Track
import com.mcasillas.dragline.ui.common.TetherDivider
import com.mcasillas.dragline.ui.theme.DraglineTheme
import com.mcasillas.dragline.ui.theme.StatusMock
import com.mcasillas.dragline.ui.theme.StatusSuccess
import com.mcasillas.dragline.ui.theme.TetherAmber
import com.mcasillas.dragline.ui.theme.TetherCyan

@Composable
fun SoundSourcesScreen(
    viewModel: SoundSourcesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    SoundSourcesScreenContent(
        uiState = uiState,
        onPreviewTrack = { viewModel.playPreview(it) },
        onSelectFallback = { viewModel.updateFallbackSound(it) },
        modifier = modifier
    )
}

@Composable
fun SoundSourcesScreenContent(
    uiState: SoundSourcesUiState,
    onPreviewTrack: (Track) -> Unit,
    onSelectFallback: (String) -> Unit,
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
            // Header
            item {
                Column {
                    Text(
                        text = "Sound Sources",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configure audio sources and your guaranteed local fallback alarm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Fallback Reliability Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Reliability Guarantee",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Streaming playback can fail due to loss of Wi-Fi, Spotify session limits, or server issues. Dragline always enforces a local fallback so your alarm never fails to sound.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Default Local Fallback Sound:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("beacon" to "Gentle Beacon", "resonance" to "Resonant Tether").forEach { (id, name) ->
                                FilterChip(
                                    selected = uiState.configuredFallbackSound == id,
                                    onClick = { onSelectFallback(id) },
                                    label = { Text(name) },
                                    leadingIcon = if (uiState.configuredFallbackSound == id) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            item { TetherDivider() }

            // Local Offline Sounds
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BUNDLED OFFLINE SOUNDS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(StatusSuccess.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Available", fontSize = 10.sp, color = StatusSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.localTracks.forEach { track ->
                            val isPlaying = uiState.currentlyPlayingTrackId == track.id
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isPlaying) TetherAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${track.artist} • Multi-tone chime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { onPreviewTrack(track) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (isPlaying) TetherAmber else TetherAmber.copy(alpha = 0.15f),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Stop Preview" else "Play Preview",
                                            tint = if (isPlaying) Color.Black else TetherAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { TetherDivider() }

            // Spotify Preview Section
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SPOTIFY ROTATING PLAYLISTS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StatusMock.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Preview / Not Connected", fontSize = 10.sp, color = StatusMock)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = StatusMock.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, StatusMock.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StatusMock,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Spotify integration is in developer preview mode. In the future, connecting Spotify will let Dragline shuffle tracks across your chosen playlists, excluding songs you heard in recent mornings to keep your alarm fresh.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mock Playlists
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.mockPlaylists.forEach { playlist ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(playlist.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${playlist.trackCount} tracks",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TetherCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        playlist.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val tracks = uiState.mockTracksByPlaylist[playlist.id] ?: emptyList()
                                    if (tracks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Sample tracks: " + tracks.take(3).joinToString(", ") { "${it.title} (${it.artist})" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
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
fun SoundSourcesPreview() {
    DraglineTheme {
        SoundSourcesScreenContent(
            uiState = SoundSourcesUiState(
                localTracks = listOf(
                    Track("beacon", "Gentle Beacon", "Dragline Audio", "Offline Essentials"),
                    Track("resonance", "Resonant Tether", "Dragline Audio", "Offline Essentials")
                ),
                mockPlaylists = listOf(
                    SoundCollection("sp_1", "Morning Indie Acoustic", "Gentle wake-up songs", 5, "spotify")
                ),
                configuredFallbackSound = "beacon",
                spotifyConnectionState = ProviderConnectionState.MOCK_MODE
            ),
            onPreviewTrack = {},
            onSelectFallback = {}
        )
    }
}
