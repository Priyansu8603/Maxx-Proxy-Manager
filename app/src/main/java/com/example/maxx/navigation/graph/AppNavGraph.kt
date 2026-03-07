package com.example.maxx.navigation.graph

import GetProScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.maxx.navigation.routes.Screen
import com.example.maxx.presentation.screens.addproxy.AddProxyScreen
import com.example.maxx.presentation.screens.browser.BrowserScreen
import com.example.maxx.presentation.screens.dashboard.HomeScreen
import com.example.maxx.presentation.screens.logs.LogsScreen
import com.example.maxx.presentation.screens.settings.AppSettingsScreen
import com.example.maxx.presentation.screens.settings.ProxySettingsScreen
import com.example.maxx.presentation.screens.settings.SettingsHomeScreen
import com.example.maxx.presentation.viewmodel.ProxyViewModel

// ─── Routes that show bottom bar ─────────────────────────────────────────────
private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Logs.route,
    Screen.SettingsHome.route,
)

// ─── Routes that show the FAB ─────────────────────────────────────────────────
private val fabRoutes = setOf(Screen.Home.route)

// ─── Nav-item descriptor ─────────────────────────────────────────────────────
private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route,"Home",Icons.Filled.Home,Icons.Outlined.Home),
    BottomNavItem(Screen.Logs.route,"Logs",Icons.AutoMirrored.Filled.Article,Icons.AutoMirrored.Outlined.Article),
    BottomNavItem(Screen.SettingsHome.route,"Settings",Icons.Filled.Settings,Icons.Outlined.Settings),
)

// ─────────────────────────────────────────────────────────────────────────────
//  AppNavGraph — ONE Scaffold, ONE BottomBar, ONE FAB
//  Industry pattern: screens are pure content composables, no nested Scaffold.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    isDarkMode: Boolean,
    toggleTheme: () -> Unit,
    onResetLanguage: () -> Unit,
) {
    val navController   = rememberNavController()
    val proxyViewModel: ProxyViewModel = hiltViewModel()
    val context         = LocalContext.current
    val cs              = MaterialTheme.colorScheme

    val backStackEntry  by navController.currentBackStackEntryAsState()
    val currentRoute    = backStackEntry?.destination?.route

    val showBottomBar   = currentRoute in bottomBarRoutes
    val showFab         = currentRoute in fabRoutes

    Scaffold(
        containerColor = cs.background,
        // ── FAB lives here — never buried inside a screen column ──
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter   = scaleIn() + fadeIn(),
                exit    = scaleOut() + fadeOut(),
            ) {
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.AddProxy.createRoute(-1)) },
                    containerColor = cs.primary,
                    contentColor   = cs.onPrimary,
                    shape          = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Proxy")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        // ── Bottom bar lives here — never inside a screen ─────────
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                AppBottomNavigationBar(
                    navController    = navController,
                    currentRoute     = currentRoute,
                    items            = bottomNavItems,
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding),
        ) {
            // ── Home ─────────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    toggleTheme   = toggleTheme,
                )
            }

            // ── Logs ──────────────────────────────────────────────────
            composable(Screen.Logs.route) {
                LogsScreen(
                    navController = navController,
                    isDarkMode    = isDarkMode,
                )
            }

            // ── Add / Edit Proxy ──────────────────────────────────────
            composable(
                route     = Screen.AddProxy.route,
                arguments = listOf(navArgument("proxyId") {
                    type         = NavType.IntType
                    defaultValue = -1
                })
            ) { backStack ->
                val proxyId     = backStack.arguments?.getInt("proxyId") ?: -1
                val proxies     by proxyViewModel.allProxies.collectAsState()
                val proxyToEdit = proxies.find { it.id == proxyId }
                AddProxyScreen(navController = navController, proxy = proxyToEdit)
            }

            // ── Settings Home ─────────────────────────────────────────
            composable(Screen.SettingsHome.route) {
                SettingsHomeScreen(
                    navController = navController,
                    isDarkMode    = isDarkMode,
                    toggleTheme   = toggleTheme,
                )
            }

            // ── App Settings ──────────────────────────────────────────
            composable(Screen.AppSettings.route) {
                AppSettingsScreen(
                    navController = navController,
                    isDarkMode    = isDarkMode,
                    toggleTheme   = toggleTheme,
                )
            }

            // ── Proxy Settings ────────────────────────────────────────
            composable(Screen.ProxySettings.route) {
                ProxySettingsScreen(
                    navController   = navController,
                    isDarkMode      = isDarkMode,
                    toggleTheme     = toggleTheme,
                    onResetLanguage = onResetLanguage,
                )
            }

            // ── Get Pro ───────────────────────────────────────────────
            composable(Screen.GetPro.route) {
                GetProScreen(navController = navController)
            }

            // ── Safe Browser ──────────────────────────────────────────
            composable(
                route     = Screen.Browser.route,
                arguments = listOf(navArgument("proxyId") {
                    type         = NavType.IntType
                    defaultValue = -1
                })
            ) { backStack ->
                val proxyId = backStack.arguments?.getInt("proxyId") ?: -1
                BrowserScreen(
                    navController = navController,
                    proxyId       = proxyId,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Bottom Navigation Bar — defined once, used once
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AppBottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    items: List<BottomNavItem>,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true; inclusive = false }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = Color.Transparent,
                ),
            )
        }
    }
}
