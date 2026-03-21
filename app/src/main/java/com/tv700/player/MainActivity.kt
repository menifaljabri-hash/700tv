package com.iptv.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iptv.player.ui.home.HomeScreen
import com.iptv.player.ui.login.AddPlaylistScreen
import com.iptv.player.ui.parental.PinGateScreen
import com.iptv.player.ui.player.PlayerScreen
import com.iptv.player.ui.theme.IPTVTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPTVTheme {
                IPTVNavGraph()
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val ADD_PLAYLIST = "add_playlist"
    const val PLAYER = "player/{streamUrl}/{title}"
    const val PIN_GATE = "pin_gate/{returnRoute}"

    fun player(streamUrl: String, title: String): String {
        val encoded = URLEncoder.encode(streamUrl, "UTF-8")
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        return "player/$encoded/$encodedTitle"
    }

    fun pinGate(returnRoute: String): String {
        val encoded = URLEncoder.encode(returnRoute, "UTF-8")
        return "pin_gate/$encoded"
    }
}

@Composable
fun IPTVNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onChannelClick = { channel ->
                    navController.navigate(Routes.player(channel.streamUrl, channel.name))
                },
                onVodClick = { movie ->
                    navController.navigate(Routes.player(movie.streamUrl, movie.name))
                }
            )
        }

        composable(Routes.ADD_PLAYLIST) {
            AddPlaylistScreen(
                onBack = { navController.popBackStack() },
                onAdded = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("title")     { type = NavType.StringType }
            )
        ) { backStack ->
            val rawUrl   = backStack.arguments?.getString("streamUrl") ?: ""
            val rawTitle = backStack.arguments?.getString("title") ?: ""
            PlayerScreen(
                streamUrl = URLDecoder.decode(rawUrl, "UTF-8"),
                title     = URLDecoder.decode(rawTitle, "UTF-8"),
                onBack    = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PIN_GATE,
            arguments = listOf(
                navArgument("returnRoute") { type = NavType.StringType }
            )
        ) { backStack ->
            val returnRoute = URLDecoder.decode(
                backStack.arguments?.getString("returnRoute") ?: "", "UTF-8"
            )
            PinGateScreen(
                onUnlocked = {
                    navController.navigate(returnRoute) { popBackStack() }
                },
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
