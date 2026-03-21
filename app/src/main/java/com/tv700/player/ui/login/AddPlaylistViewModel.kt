package com.iptv.player.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.repository.PlaylistDataStore
import com.iptv.player.domain.model.Playlist
import com.iptv.player.domain.model.PlaylistType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPlaylistViewModel @Inject constructor(
    private val playlistStore: PlaylistDataStore,
    private val xtreamApi: XtreamApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun addPlaylist(playlist: Playlist, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Validate Xtream credentials before saving
                if (playlist.type == PlaylistType.XTREAM) {
                    val info = xtreamApi.getServerInfo(playlist.username, playlist.password)
                    if (info.userInfo.status != "Active") {
                        _error.value = "Account status: ${info.userInfo.status}"
                        return@launch
                    }
                }
                playlistStore.addPlaylist(playlist)
                playlistStore.setActivePlaylist(playlist.id)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Connection failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
