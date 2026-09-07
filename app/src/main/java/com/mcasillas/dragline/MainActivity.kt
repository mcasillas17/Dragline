package com.mcasillas.dragline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mcasillas.dragline.domain.repository.SettingsRepository
import com.mcasillas.dragline.ui.navigation.DraglineNavHost
import com.mcasillas.dragline.ui.theme.DraglineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by settingsRepository.getSettings().collectAsState(
                initial = com.mcasillas.dragline.domain.model.UserSettings()
            )

            DraglineTheme(appTheme = settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DraglineNavHost()
                }
            }
        }
    }
}
