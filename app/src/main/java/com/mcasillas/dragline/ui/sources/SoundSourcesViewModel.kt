package com.mcasillas.dragline.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcasillas.dragline.audio.AudioPlayer
import com.mcasillas.dragline.audio.FakeSpotifyProvider
import com.mcasillas.dragline.audio.LocalSoundProvider
import com.mcasillas.dragline.audio.ProviderConnectionState
import com.mcasillas.dragline.audio.SoundCollection
import com.mcasillas.dragline.domain.model.Track
import com.mcasillas.dragline.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SoundSourcesUiState(
    val localTracks: List<Track> = emptyList(),
    val mockPlaylists: List<SoundCollection> = emptyList(),
    val mockTracksByPlaylist: Map<String, List<Track>> = emptyMap(),
    val configuredFallbackSound: String = "beacon",
    val currentlyPlayingTrackId: String? = null,
    val spotifyConnectionState: ProviderConnectionState = ProviderConnectionState.MOCK_MODE
)

@HiltViewModel
class SoundSourcesViewModel @Inject constructor(
    private val localSoundProvider: LocalSoundProvider,
    private val fakeSpotifyProvider: FakeSpotifyProvider,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoundSourcesUiState())
    val uiState: StateFlow<SoundSourcesUiState> = _uiState.asStateFlow()

    init {
        loadSources()
        observeSettings()
    }

    private fun loadSources() {
        viewModelScope.launch {
            val localTracks = localSoundProvider.getTracks("local_builtins")
            val playlists = fakeSpotifyProvider.listCollections()
            val tracksMap = playlists.associate { it.id to fakeSpotifyProvider.getTracks(it.id) }

            _uiState.update {
                it.copy(
                    localTracks = localTracks,
                    mockPlaylists = playlists,
                    mockTracksByPlaylist = tracksMap,
                    spotifyConnectionState = fakeSpotifyProvider.connectionState
                )
            }
        }
    }

    private fun observeSettings() {
        settingsRepository.getSettings()
            .onEach { settings ->
                _uiState.update { it.copy(configuredFallbackSound = settings.defaultFallbackSound) }
            }
            .launchIn(viewModelScope)
    }

    fun playPreview(track: Track) {
        val current = _uiState.value.currentlyPlayingTrackId
        if (current == track.id) {
            stopPreview()
        } else {
            val resId = localSoundProvider.getLocalTrackResource(track.id)
            audioPlayer.playRaw(resId)
            _uiState.update { it.copy(currentlyPlayingTrackId = track.id) }
        }
    }

    fun stopPreview() {
        audioPlayer.stop()
        _uiState.update { it.copy(currentlyPlayingTrackId = null) }
    }

    fun updateFallbackSound(soundId: String) {
        viewModelScope.launch {
            settingsRepository.updateDefaultFallbackSound(soundId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
