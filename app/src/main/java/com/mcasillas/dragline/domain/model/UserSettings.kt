package com.mcasillas.dragline.domain.model

enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    DARK("Dark"),
    LIGHT("Light")
}

data class UserSettings(
    val theme: AppTheme = AppTheme.DARK,
    val defaultFallbackSound: String = "beacon",
    val defaultWakeChallenge: WakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
    val recentSongExclusionDays: Int = 14,
    val gradualVolumeDurationSeconds: Int = 60,
    val isVibrationEnabled: Boolean = true
)
