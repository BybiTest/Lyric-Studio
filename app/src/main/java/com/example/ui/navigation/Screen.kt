package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Projects : Screen("projects")
    object Editor : Screen("editor/{projectId}") {
        fun createRoute(projectId: Long): String = "editor/$projectId"
    }
    object QuickWriting : Screen("quick_writing")
    object Ideas : Screen("ideas")
    object Punchlines : Screen("punchlines")
    object WordBank : Screen("word_bank")
    object Tools : Screen("tools")
    object Metronome : Screen("metronome")
    object VoiceMemos : Screen("voice_memos")
    object RhymeFinder : Screen("rhyme_finder")
    object Templates : Screen("templates")
    object Goals : Screen("goals")
    object StreakCalendar : Screen("streak_calendar")
    object VersionHistory : Screen("version_history/{projectId}") {
        fun createRoute(projectId: Long): String = "version_history/$projectId"
    }
    object Trash : Screen("trash")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object AppLock : Screen("app_lock")
}

data class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        titleRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Projects.route,
        titleRes = R.string.nav_projects,
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    ),
    BottomNavItem(
        route = Screen.Ideas.route,
        titleRes = R.string.nav_ideas,
        selectedIcon = Icons.Filled.Lightbulb,
        unselectedIcon = Icons.Outlined.Lightbulb
    ),
    BottomNavItem(
        route = Screen.Tools.route,
        titleRes = R.string.nav_tools,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        titleRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
