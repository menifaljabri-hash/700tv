package com.iptv.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.repository.IptvRepository
import com.iptv.player.domain.model.Channel
import com.iptv.player.domain.model.Playlist
import com.iptv.player.domain.model.Resource
import com.iptv.player.domain.model.VodMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: IptvRepository,
    private val playlistStore: com.iptv.player.data.repository.PlaylistDataStore
) : ViewModel() {

    private val _liveChannels = MutableStateFlow<Resource<List<Channel>>>(Resource.Loading)
    val liveChannels: StateFlow<Resource<List<Channel>>> = _liveChannels.asStateFlow()

    private val _vodMovies = MutableStateFlow<Resource<List<VodMovie>>>(Resource.Loading)
    val vodMovies: StateFlow<Resource<List<VodMovie>>> = _vodMovies.asStateFlow()

    init {
        loadContent()
    }

    fun refresh() = loadContent()

    private fun loadContent() {
        viewModelScope.launch {
            val playlist = playlistStore.getActivePlaylist() ?: return@launch

            repository.getLiveChannels(playlist)
                .collect { _liveChannels.value = it }
        }
        viewModelScope.launch {
            val playlist = playlistStore.getActivePlaylist() ?: return@launch

            repository.getVodMovies(playlist)
                .collect { _vodMovies.value = it }
        }
    }
}
