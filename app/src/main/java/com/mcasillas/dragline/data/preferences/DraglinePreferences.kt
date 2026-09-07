package com.mcasillas.dragline.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.mcasillas.dragline.domain.model.AppTheme
import com.mcasillas.dragline.domain.model.UserSettings
import com.mcasillas.dragline.domain.model.WakeChallengeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraglinePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "dragline_user_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_FALLBACK_SOUND = "pref_fallback_sound"
        private const val KEY_WAKE_CHALLENGE = "pref_wake_challenge"
        private const val KEY_EXCLUSION_DAYS = "pref_exclusion_days"
        private const val KEY_GRADUAL_VOLUME_SECONDS = "pref_gradual_volume_seconds"
        private const val KEY_VIBRATION_ENABLED = "pref_vibration_enabled"
    }

    fun getSettingsFlow(): Flow<UserSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getCurrentSettings())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        // Emit initial value
        trySend(getCurrentSettings())

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged().conflate()

    fun getCurrentSettings(): UserSettings {
        val themeString = prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name
        val theme = try {
            AppTheme.valueOf(themeString)
        } catch (e: Exception) {
            AppTheme.DARK
        }

        val fallbackSound = prefs.getString(KEY_FALLBACK_SOUND, "beacon") ?: "beacon"
        val challengeString = prefs.getString(KEY_WAKE_CHALLENGE, WakeChallengeType.HOLD_TO_WAKE.name)
            ?: WakeChallengeType.HOLD_TO_WAKE.name
        val challenge = try {
            WakeChallengeType.valueOf(challengeString)
        } catch (e: Exception) {
            WakeChallengeType.HOLD_TO_WAKE
        }

        val exclusionDays = prefs.getInt(KEY_EXCLUSION_DAYS, 14)
        val gradualVolume = prefs.getInt(KEY_GRADUAL_VOLUME_SECONDS, 60)
        val vibration = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)

        return UserSettings(
            theme = theme,
            defaultFallbackSound = fallbackSound,
            defaultWakeChallenge = challenge,
            recentSongExclusionDays = exclusionDays,
            gradualVolumeDurationSeconds = gradualVolume,
            isVibrationEnabled = vibration
        )
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun setDefaultFallbackSound(sound: String) {
        prefs.edit().putString(KEY_FALLBACK_SOUND, sound).apply()
    }

    fun setDefaultWakeChallenge(challenge: WakeChallengeType) {
        prefs.edit().putString(KEY_WAKE_CHALLENGE, challenge.name).apply()
    }

    fun setRecentSongExclusionDays(days: Int) {
        prefs.edit().putInt(KEY_EXCLUSION_DAYS, days).apply()
    }

    fun setGradualVolumeDuration(seconds: Int) {
        prefs.edit().putInt(KEY_GRADUAL_VOLUME_SECONDS, seconds).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }
}
