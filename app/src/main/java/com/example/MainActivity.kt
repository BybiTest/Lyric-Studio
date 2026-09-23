package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.domain.model.StudioTheme
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.goals.GoalsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.ideas.IdeasScreen
import com.example.ui.screens.lock.AppLockScreen
import com.example.ui.screens.metronome.MetronomeScreen
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.screens.punchlines.PunchlinesScreen
import com.example.ui.screens.quick.QuickWritingScreen
import com.example.ui.screens.rhyme.RhymeScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.streak.StreakScreen
import com.example.ui.screens.templates.TemplatesScreen
import com.example.ui.screens.tools.ToolsScreen
import com.example.ui.screens.trash.TrashScreen
import com.example.ui.screens.versions.VersionHistoryScreen
import com.example.ui.screens.voice.VoiceScreen
import com.example.ui.screens.wordbank.WordBankScreen
import com.example.ui.theme.LyricStudioTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private var isUnlocked by mutableStateOf(false)

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lang = prefs.getString("language", "fa") ?: "fa"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUnlocked = savedInstanceState?.getBoolean("is_unlocked", false) ?: false
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val initialLang = prefs.getString("language", "fa") ?: "fa"
        if (initialLang == "fa") {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }

        setContent {
            val app = LyricStudioApp.instance
            val themeId by app.preferencesManager.themeFlow.collectAsState(initial = "midnight")
            val currentLang by app.preferencesManager.languageFlow.collectAsState(initial = initialLang)
            val isAppLockEnabled by app.preferencesManager.appLockEnabled.collectAsState(initial = false)
            val studioTheme = remember(themeId) { StudioTheme.fromId(themeId) }

            LaunchedEffect(currentLang) {
                window.decorView.layoutDirection = if (currentLang == "fa") {
                    View.LAYOUT_DIRECTION_RTL
                } else {
                    View.LAYOUT_DIRECTION_LTR
                }
            }

            LyricStudioTheme(studioTheme = studioTheme, language = currentLang) {
                if (isAppLockEnabled && !isUnlocked) {
                    AppLockScreen(onUnlocked = { isUnlocked = true })
                } else {
                    LyricStudioAppRoot()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_unlocked", isUnlocked)
    }
}

@Composable
fun LyricStudioAppRoot() {
    var hasPassedSplash by remember { mutableStateOf(false) }

    if (!hasPassedSplash) {
        SplashScreen(onSplashFinished = { hasPassedSplash = true })
    } else {
        MainNavigationGraph()
    }
}

@Composable
fun MainNavigationGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
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
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(item.titleRes),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            // Main tabs
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProject = { id ->
                        navController.navigate(Screen.Editor.createRoute(id))
                    },
                    onNavigateToProjects = {
                        navController.navigate(Screen.Projects.route)
                    },
                    onNavigateToQuickWriting = {
                        navController.navigate(Screen.QuickWriting.route)
                    },
                    onNavigateToIdeas = {
                        navController.navigate(Screen.Ideas.route)
                    },
                    onNavigateToVoice = {
                        navController.navigate(Screen.VoiceMemos.route)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            composable(Screen.Projects.route) {
                ProjectsScreen(
                    onNavigateToProject = { id ->
                        navController.navigate(Screen.Editor.createRoute(id))
                    }
                )
            }

            composable(Screen.Ideas.route) {
                IdeasScreen(
                    onNavigateToProject = { id ->
                        navController.navigate(Screen.Editor.createRoute(id))
                    }
                )
            }

            composable(Screen.Tools.route) {
                ToolsScreen(
                    onNavigateToRoute = { route ->
                        navController.navigate(route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToTrash = {
                        navController.navigate(Screen.Trash.route)
                    }
                )
            }

            // Editor
            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                EditorScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMetronome = { navController.navigate(Screen.Metronome.route) },
                    onNavigateToRhymes = { navController.navigate(Screen.RhymeFinder.route) },
                    onNavigateToVersions = { id -> navController.navigate(Screen.VersionHistory.createRoute(id)) }
                )
            }

            // Quick Writing
            composable(Screen.QuickWriting.route) {
                QuickWritingScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProject = { id ->
                        navController.navigate(Screen.Editor.createRoute(id)) {
                            popUpTo(Screen.QuickWriting.route) { inclusive = true }
                        }
                    }
                )
            }

            // Metronome
            composable(Screen.Metronome.route) {
                MetronomeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Voice Memos
            composable(Screen.VoiceMemos.route) {
                VoiceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Rhyme Finder
            composable(Screen.RhymeFinder.route) {
                RhymeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Word Bank
            composable(Screen.WordBank.route) {
                WordBankScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Punchlines
            composable(Screen.Punchlines.route) {
                PunchlinesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Templates
            composable(Screen.Templates.route) {
                TemplatesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProject = { id ->
                        navController.navigate(Screen.Editor.createRoute(id))
                    }
                )
            }

            // Goals
            composable(Screen.Goals.route) {
                GoalsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Streak & Calendar
            composable(Screen.StreakCalendar.route) {
                StreakScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Version History
            composable(
                route = Screen.VersionHistory.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                VersionHistoryScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Trash
            composable(Screen.Trash.route) {
                TrashScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Search
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProject = { id ->
                        navController.navigate(Screen.Editor.createRoute(id))
                    }
                )
            }
        }
    }
}
