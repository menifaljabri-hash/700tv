package com.tv700.player.ui.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tv700.player.R
import com.tv700.player.data.download.DownloadStatus
import com.tv700.player.domain.model.VodMovie
import com.tv700.player.ui.theme.*

@Composable
fun VodDetailScreen(
    movie: VodMovie,
    onPlay: () -> Unit,
    onPlayOffline: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: VodDetailViewModel = hiltViewModel()
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    LaunchedEffect(movie.id) {
        viewModel.checkDownloadStatus(movie.id)
    }

    Box(Modifier.fillMaxSize().background(Navy900)) {
        // Backdrop
        if (movie.backdropUrl.isNotBlank()) {
            AsyncImage(
                model = movie.backdropUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(280.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxWidth().height(280.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Navy900)))
            )
        }

        Column(Modifier.verticalScroll(rememberScrollState())) {
            // Back button
            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp).statusBarsPadding()) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.height(160.dp))

            // Content
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.Bottom) {
                // Poster
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.name,
                    modifier = Modifier.width(110.dp).height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(movie.name, color = SharkWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    if (movie.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 12.sp)
                            Text(" %.1f".format(movie.rating), color = Gold500, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (movie.releaseDate.isNotBlank()) {
                        Text(movie.releaseDate, color = SharkGray, fontSize = 12.sp)
                    }
                    if (movie.duration.isNotBlank()) {
                        Text(movie.duration, color = SharkGray, fontSize = 12.sp)
                    }
                    if (movie.genre.isNotBlank()) {
                        Text(movie.genre, color = Gold500.copy(0.8f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play button
                Button(
                    onClick = onPlay,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Navy900)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.play), color = Navy900, fontWeight = FontWeight.Bold)
                }

                // Download button
                when (downloadState) {
                    DownloadStatus.COMPLETE -> {
                        // Play offline
                        OutlinedButton(
                            onClick = { /* get local path from viewModel */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = Gold500),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OfflinePin, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.play_offline), fontSize = 12.sp)
                        }
                    }
                    DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                        // Progress button
                        OutlinedButton(
                            onClick = { viewModel.cancelDownload(movie.id) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = SharkGray),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Navy500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.size(18.dp),
                                color = Gold500,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("$downloadProgress%", fontSize = 12.sp)
                        }
                    }
                    else -> {
                        // Download
                        OutlinedButton(
                            onClick = { viewModel.startDownload(movie) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = SharkWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Navy500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.download), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Plot
            if (movie.plot.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Synopsis", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(movie.plot, color = SharkGray, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }

            if (movie.director.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Director", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(movie.director, color = SharkGray, fontSize = 13.sp)
                }
            }

            if (movie.actors.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Cast", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(movie.actors, color = SharkGray, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
