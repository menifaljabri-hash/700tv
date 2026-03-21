package com.tv700.player.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tv700.player.domain.model.Channel
import com.tv700.player.domain.model.Resource
import com.tv700.player.domain.model.VodMovie
import com.tv700.player.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChannelClick: (Channel) -> Unit,
    onVodClick: (VodMovie) -> Unit,
    onAddPlaylist: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📺  Live TV", "🎬  Movies")

    val liveState by viewModel.liveChannels.collectAsState()
    val vodState  by viewModel.vodMovies.collectAsState()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Navy800, Navy700, Navy800))
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + name
                    AsyncImage(
                        model = "file:///android_asset/logo_700tv.png",
                        contentDescription = "700TV",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "700TV",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold500,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Gold500)
                    }
                    IconButton(onClick = onAddPlaylist) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Gold500)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SharkGray)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Navy900)
        ) {
            // Custom tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) Brush.horizontalGradient(listOf(Gold600, Gold500))
                                else Brush.horizontalGradient(listOf(Navy700, Navy700))
                            )
                            .border(
                                width = if (selected) 0.dp else 1.dp,
                                color = if (selected) Color.Transparent else Navy500,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickableNoRipple { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Navy900 else SharkGray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> ContentPanel(state = liveState) { channels ->
                    ChannelGrid(channels = channels, onChannelClick = onChannelClick)
                }
                1 -> ContentPanel(state = vodState) { movies ->
                    VodGrid(movies = movies, onVodClick = onVodClick)
                }
            }
        }
    }
}

@Composable
private fun <T> ContentPanel(state: Resource<T>, content: @Composable (T) -> Unit) {
    when (state) {
        is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Gold500)
                Spacer(Modifier.height(12.dp))
                Text("جاري التحميل...", color = SharkGray, fontSize = 14.sp)
            }
        }
        is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("⚠️", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
        is Resource.Success -> content(state.data)
    }
}

@Composable
private fun ChannelGrid(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Navy500)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (channel.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                        .background(Navy600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(channel.name.take(2).uppercase(), color = Gold500, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.labelSmall,
                color = SharkWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VodGrid(movies: List<VodMovie>, onVodClick: (VodMovie) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(movies, key = { it.id }) { movie ->
            VodCard(movie = movie, onClick = { onVodClick(movie) })
        }
    }
}

@Composable
private fun VodCard(movie: VodMovie, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(0.67f),
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Navy900.copy(alpha = 0.92f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = movie.name,
                        color = SharkWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (movie.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 9.sp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "%.1f".format(movie.rating),
                                color = Gold500,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper extension to avoid ripple on custom tab
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.androidx.compose.foundation.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            onClick = onClick
        )
    )
