package com.mcasillas.dragline.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Alarms : TopLevelDestination("alarms", "Alarms", Icons.Default.Alarm)
    data object SoundSources : TopLevelDestination("sources", "Sources", Icons.Default.MusicNote)
    data object Settings : TopLevelDestination("settings", "Settings", Icons.Default.Settings)

    companion object {
        val ALL = listOf(Alarms, SoundSources, Settings)
    }
}

object NavRoutes {
    const val ALARMS = "alarms"
    const val SOURCES = "sources"
    const val SETTINGS = "settings"
    const val EDITOR = "editor?alarmId={alarmId}"

    fun editorRoute(alarmId: Long? = null): String {
        return if (alarmId != null) "editor?alarmId=$alarmId" else "editor"
    }
}
