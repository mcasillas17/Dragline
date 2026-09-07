package com.mcasillas.dragline.domain.repository

import com.mcasillas.dragline.domain.model.AppTheme
import com.mcasillas.dragline.domain.model.UserSettings
import com.mcasillas.dragline.domain.model.WakeChallengeType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateDefaultFallbackSound(sound: String)
    suspend fun updateDefaultWakeChallenge(challenge: WakeChallengeType)
    suspend fun updateRecentSongExclusionDays(days: Int)
    suspend fun updateGradualVolumeDuration(seconds: Int)
    suspend fun updateVibrationEnabled(enabled: Boolean)
}
