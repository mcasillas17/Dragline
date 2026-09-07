package com.mcasillas.dragline.data.repository

import com.mcasillas.dragline.data.preferences.DraglinePreferences
import com.mcasillas.dragline.domain.model.AppTheme
import com.mcasillas.dragline.domain.model.UserSettings
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: DraglinePreferences
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings> {
        return preferences.getSettingsFlow()
    }

    override suspend fun updateTheme(theme: AppTheme) {
        preferences.setTheme(theme)
    }

    override suspend fun updateDefaultFallbackSound(sound: String) {
        preferences.setDefaultFallbackSound(sound)
    }

    override suspend fun updateDefaultWakeChallenge(challenge: WakeChallengeType) {
        preferences.setDefaultWakeChallenge(challenge)
    }

    override suspend fun updateRecentSongExclusionDays(days: Int) {
        preferences.setRecentSongExclusionDays(days)
    }

    override suspend fun updateGradualVolumeDuration(seconds: Int) {
        preferences.setGradualVolumeDuration(seconds)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        preferences.setVibrationEnabled(enabled)
    }
}
