package com.tv700.player.ui.downloads

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tv700.player.R
import com.tv700.player.data.download.DownloadItem
import com.tv700.player.data.download.DownloadStatus
import com.tv700.player.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onPlayOffline: (String) -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsState()
    val progress  by viewModel.progressMap.collectAsState()
    var deleteTarget by remember { mutableStateOf<DownloadItem?>(null) }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Navy800, Navy700, Navy800)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Gold500, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.downloads_title),
                        color = Gold500, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    if (downloads.isNotEmpty()) {
                        Text(
                            "${downloads.size} ${if (downloads.size == 1) "file" else "files"}",
                            color = SharkGray, fontSize = 12.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (downloads.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DownloadDone, contentDescription = null,
                        tint = Navy600, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.no_downloads), color = SharkGray, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Go to Movies to download", color = Navy500, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(downloads, key = { it.id }) { item ->
                    DownloadCard(
                        item      = item,
                        progress  = progress[item.id] ?: 0,
                        onPlay    = { onPlayOffline(item.localPath) },
                        onDelete  = { deleteTarget = item }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = Navy700,
            title = { Text(stringResource(R.string.confirm_delete), color = SharkWhite) },
            text  = { Text(item.title, color = SharkGray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDownload(item)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel), color = Gold500)
                }
            }
        )
    }
}

@Composable
private fun DownloadCard(
    item: DownloadItem,
    progress: Int,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape  = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy600)
            ) {
                if (item.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Status badge
                val (badgeColor, badgeIcon) = when (item.status) {
                    DownloadStatus.COMPLETE    -> Gold500 to Icons.Default.CheckCircle
                    DownloadStatus.FAILED      -> Color(0xFFCF6679) to Icons.Default.Error
                    DownloadStatus.DOWNLOADING -> NavyBlue to Icons.Default.Downloading
                    DownloadStatus.PAUSED      -> SharkGray to Icons.Default.Pause
                    DownloadStatus.QUEUED      -> SharkGray to Icons.Default.HourglassBottom
                }
                Box(
                    Modifier.align(Alignment.BottomEnd).padding(4.dp)
                        .size(20.dp).clip(RoundedCornerShape(10.dp))
                        .background(Navy900.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(badgeIcon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = SharkWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                when (item.status) {
                    DownloadStatus.DOWNLOADING -> {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = Gold500,
                            trackColor = Navy500
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("$progress%", color = Gold500, fontSize = 11.sp)
                    }
                    DownloadStatus.COMPLETE -> {
                        Text(stringResource(R.string.download_complete), color = Gold500, fontSize = 11.sp)
                        if (item.fileSizeBytes > 0) {
                            Text(formatFileSize(item.fileSizeBytes), color = SharkGray, fontSize = 11.sp)
                        }
                    }
                    DownloadStatus.FAILED -> {
                        Text(stringResource(R.string.download_failed), color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                    DownloadStatus.QUEUED -> {
                        Text("Queued", color = SharkGray, fontSize = 11.sp)
                    }
                    else -> {}
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Play button (only if complete)
                if (item.status == DownloadStatus.COMPLETE) {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.radialGradient(listOf(Gold500, Gold700)))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play_offline),
                            tint = Navy900, modifier = Modifier.size(22.dp))
                    }
                }
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Navy600)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_download),
                        tint = Color(0xFFCF6679), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L         -> "%.1f KB".format(bytes / 1_024.0)
    else                    -> "$bytes B"
}
