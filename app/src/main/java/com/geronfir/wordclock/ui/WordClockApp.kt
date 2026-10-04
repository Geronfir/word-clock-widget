package com.geronfir.wordclock.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geronfir.wordclock.R

/**
 * The three top-level destinations of the app.
 *
 * A data holder so the navigation bar and the NavHost can never drift apart:
 * both are generated from this one list.
 */
private data class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val Destinations = listOf(
    TopLevelDestination(
        route = WordClockRoutes.HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    TopLevelDestination(
        route = WordClockRoutes.DISPLAY,
        labelRes = R.string.nav_display,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
    TopLevelDestination(
        route = WordClockRoutes.ABOUT,
        labelRes = R.string.nav_about,
        selectedIcon = Icons.Filled.Info,
        unselectedIcon = Icons.Outlined.Info,
    ),
)

/** Route names, shared between [WordClockApp] and the screens' previews/tests. */
object WordClockRoutes {
    const val HOME = "home"
    const val DISPLAY = "display"
    const val ABOUT = "about"
}

/**
 * App shell: Material 3 [Scaffold] with a bottom [NavigationBar] and a
 * [NavHost] for the three top-level destinations.
 *
 * Navigation uses `saveState/restoreState` so switching tabs keeps each
 * screen's state — standard single-activity pattern from the Navigation
 * Compose docs.
 */
@Composable
fun WordClockApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Pop up to the start destination to avoid
                                // building a growing back stack of tabs.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) destination.selectedIcon
                                else destination.unselectedIcon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WordClockRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(WordClockRoutes.HOME) { HomeScreen() }
            composable(WordClockRoutes.DISPLAY) { DisplayScreen() }
            composable(WordClockRoutes.ABOUT) { AboutScreen() }
        }
    }
}
