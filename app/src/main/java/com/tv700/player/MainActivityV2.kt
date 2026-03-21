package com.tv700.player

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tv700.player.data.locale.LanguageManager
import com.tv700.player.ui.downloads.DownloadsScreen
import com.tv700.player.ui.home.HomeScreen
import com.tv700.player.ui.login.AddPlaylistScreen
import com.tv700.player.ui.parental.PinGateScreen
import com.tv700.player.ui.player.PlayerScreen
import com.tv700.player.ui.settings.SettingsScreen
import com.tv700.player.ui.splash.SplashScreen
import com.tv700.player.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var languageManager: LanguageManager

    // Apply locale before view inflation
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(languageManager.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TV700Theme {
                TV700App()
            }
        }
    }
}

// ── Bottom nav items ──────────────────────────────────────────────────────────

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
)

val bottomNavItems = listOf(
    BottomNavItem("home",      Icons.Default.LiveTv,     R.string.nav_live_tv),
    BottomNavItem("downloads", Icons.Default.Download,   R.string.nav_downloads),
    BottomNavItem("settings",  Icons.Default.Settings,   R.string.nav_settings)
)

// ── Route constants ───────────────────────────────────────────────────────────

object Routes {
    const val SPLASH       = "splash"
    const val HOME         = "home"
    const val ADD_PLAYLIST = "add_playlist"
    const val DOWNLOADS    = "downloads"
    const val SETTINGS     = "settings"
    const val PLAYER       = "player/{streamUrl}/{title}"
    const val PIN_GATE     = "pin_gate"

    fun player(url: String, title: String) =
        "player/${URLEncoder.encode(url, "UTF-8")}/${URLEncoder.encode(title, "UTF-8")}"
}

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun TV700App() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.DOWNLOADS, Routes.SETTINGS)

    Scaffold(
        containerColor = Navy900,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Navy800, tonalElevation = 0.dp) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = stringResource(item.labelRes),
                                    tint = if (selected) Gold500 else SharkGray)
                            },
                            label = {
                                Text(stringResource(item.labelRes),
                                    color = if (selected) Gold500 else SharkGray)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Navy700
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = Routes.SPLASH) {

                composable(Routes.SPLASH) {
                    SplashScreen(onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    })
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        onChannelClick  = { ch -> navController.navigate(Routes.player(ch.streamUrl, ch.name)) },
                        onVodClick      = { vod -> navController.navigate(Routes.player(vod.streamUrl, vod.name)) },
                        onAddPlaylist   = { navController.navigate(Routes.ADD_PLAYLIST) },
                        onSettings      = { navController.navigate(Routes.SETTINGS) }
                    )
                }

                composable(Routes.ADD_PLAYLIST) {
                    AddPlaylistScreen(
                        onBack  = { navController.popBackStack() },
                        onAdded = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.DOWNLOADS) {
                    DownloadsScreen(
                        onPlayOffline = { localPath ->
                            navController.navigate(Routes.player(localPath, "Offline"))
                        }
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route     = Routes.PLAYER,
                    arguments = listOf(
                        navArgument("streamUrl") { type = NavType.StringType },
                        navArgument("title")     { type = NavType.StringType }
                    )
                ) { back ->
                    PlayerScreen(
                        streamUrl = URLDecoder.decode(back.arguments?.getString("streamUrl") ?: "", "UTF-8"),
                        title     = URLDecoder.decode(back.arguments?.getString("title") ?: "", "UTF-8"),
                        onBack    = { navController.popBackStack() }
                    )
                }

                composable(Routes.PIN_GATE) {
                    PinGateScreen(
                        onUnlocked = { navController.popBackStack() },
                        onDismiss  = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
