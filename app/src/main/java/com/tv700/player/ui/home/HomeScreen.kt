package com.iptv.player.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.iptv.player.domain.model.Channel
import com.iptv.player.domain.model.Resource
import com.iptv.player.domain.model.VodMovie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChannelClick: (Channel) -> Unit,
    onVodClick: (VodMovie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Live TV", "Movies")

    val liveState by viewModel.liveChannels.collectAsState()
    val vodState by viewModel.vodMovies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IPTV Player") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { /* settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            TabRow(
                selectedTabIndex = selectedTab,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
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
private fun <T> ContentPanel(
    state: Resource<T>,
    content: @Composable (T) -> Unit
) {
    when (state) {
        is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
        }
        is Resource.Success -> content(state.data)
    }
}

@Composable
private fun ChannelGrid(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                modifier = Modifier.size(56.dp),
                error = null
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VodGrid(movies: List<VodMovie>, onVodClick: (VodMovie) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(movies, key = { it.id }) { movie ->
            VodCard(movie = movie, onClick = { onVodClick(movie) })
        }
    }
}

@Composable
private fun VodCard(movie: VodMovie, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(0.67f)) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = movie.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
