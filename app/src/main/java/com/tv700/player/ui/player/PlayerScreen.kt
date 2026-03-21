package com.iptv.player.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    title: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(streamUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4_000)
            showControls = false
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        // ── Video surface ─────────────────────────────────────────────────
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false   // custom controls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Buffering indicator ───────────────────────────────────────────
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ── Overlay controls ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerControls(
                title = title,
                isPlaying = isPlaying,
                onBack = onBack,
                onPlayPause = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onRewind = { exoPlayer.seekBack() },
                onFastForward = { exoPlayer.seekForward() }
            )
        }
    }
}

@Composable
private fun PlayerControls(
    title: String,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onFastForward: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(text = title, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }

        // Center controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRewind, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.Replay10, contentDescription = "Rewind", tint = Color.White, modifier = Modifier.fillMaxSize())
            }
            IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(onClick = onFastForward, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.Forward10, contentDescription = "Forward", tint = Color.White, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
