package com.tv700.player.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tv700.player.R
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
    val liveState by viewModel.liveChannels.collectAsState()
    val vodState  by viewModel.vodMovies.collectAsState()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Navy800, Navy700, Navy800)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("700TV", color = Gold500, fontWeight = FontWeight.Black,
                        fontSize = 22.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Gold500)
                    }
                    IconButton(onClick = onAddPlaylist) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Gold500)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = SharkGray)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().background(Navy900)
        ) {
            // Tab row
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    stringResource(R.string.nav_live_tv),
                    stringResource(R.string.nav_movies)
                ).forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) Brush.horizontalGradient(listOf(Gold600, Gold500))
                                else Brush.horizontalGradient(listOf(Navy700, Navy700))
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { selectedTab = index }
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
                0 -> ContentPanel(liveState) { channels ->
                    ChannelGrid(channels, onChannelClick)
                }
                1 -> ContentPanel(vodState) { movies ->
                    VodGrid(movies, onVodClick)
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
                Text("Loading…", color = SharkGray, fontSize = 14.sp)
            }
        }
        is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(state.message, color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
        }
        is Resource.Success -> content(state.data)
    }
}

@Composable
private fun ChannelGrid(channels: List<Channel>, onClick: (Channel) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(channels, key = { it.id }) { ch ->
            Card(
                onClick = { onClick(ch) },
                modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
                colors = CardDefaults.cardColors(containerColor = Navy700),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    Modifier.padding(8.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (ch.logoUrl.isNotBlank()) {
                        AsyncImage(model = ch.logoUrl, contentDescription = ch.name,
                            modifier = Modifier.size(52.dp), contentScale = ContentScale.Fit)
                    } else {
                        Box(Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                            .background(Navy600), Alignment.Center) {
                            Text(ch.name.take(2).uppercase(), color = Gold500,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(ch.name, color = SharkWhite, fontSize = 11.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun VodGrid(movies: List<VodMovie>, onClick: (VodMovie) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(movies, key = { it.id }) { movie ->
            Card(
                onClick = { onClick(movie) },
                modifier = Modifier.fillMaxWidth().aspectRatio(0.67f),
                colors = CardDefaults.cardColors(containerColor = Navy700),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(model = movie.posterUrl, contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(
                        Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(
                                listOf(Color.Transparent, Navy900.copy(alpha = 0.92f))))
                            .padding(8.dp)
                    ) {
                        Text(movie.name, color = SharkWhite, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
