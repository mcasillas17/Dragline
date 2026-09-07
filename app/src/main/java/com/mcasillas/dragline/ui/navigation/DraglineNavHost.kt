package com.mcasillas.dragline.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mcasillas.dragline.ui.alarms.AlarmsScreen
import com.mcasillas.dragline.ui.alarms.AlarmsViewModel
import com.mcasillas.dragline.ui.editor.AlarmEditorScreen
import com.mcasillas.dragline.ui.editor.AlarmEditorViewModel
import com.mcasillas.dragline.ui.settings.SettingsScreen
import com.mcasillas.dragline.ui.settings.SettingsViewModel
import com.mcasillas.dragline.ui.sources.SoundSourcesScreen
import com.mcasillas.dragline.ui.sources.SoundSourcesViewModel
import com.mcasillas.dragline.ui.theme.TetherAmber
import com.mcasillas.dragline.ui.theme.TetherCyan

@Composable
fun DraglineNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = TopLevelDestination.ALL.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    TopLevelDestination.ALL.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TetherAmber,
                                selectedTextColor = TetherAmber,
                                indicatorColor = TetherAmber.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.ALARMS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.ALARMS) {
                val viewModel: AlarmsViewModel = hiltViewModel()
                AlarmsScreen(
                    viewModel = viewModel,
                    onNavigateToEditor = { alarmId ->
                        navController.navigate(NavRoutes.editorRoute(alarmId))
                    }
                )
            }

            composable(NavRoutes.SOURCES) {
                val viewModel: SoundSourcesViewModel = hiltViewModel()
                SoundSourcesScreen(viewModel = viewModel)
            }

            composable(NavRoutes.SETTINGS) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = viewModel)
            }

            composable(
                route = NavRoutes.EDITOR,
                arguments = listOf(
                    navArgument("alarmId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val alarmId = backStackEntry.arguments?.getLong("alarmId")
                val viewModel: AlarmEditorViewModel = hiltViewModel()
                AlarmEditorScreen(
                    viewModel = viewModel,
                    alarmId = if (alarmId == 0L) null else alarmId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
