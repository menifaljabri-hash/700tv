package com.tv700.player.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tv700.player.data.download.DownloadItem
import com.tv700.player.data.download.DownloadStatus
import com.tv700.player.data.download.VodDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadManager: VodDownloadManager
) : ViewModel() {

    val downloads: StateFlow<List<DownloadItem>> = downloadManager.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _progressMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val progressMap = _progressMap.asStateFlow()

    init {
        // Poll progress every 1 second for active downloads
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                val active = downloads.value.filter {
                    it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED
                }
                if (active.isNotEmpty()) {
                    val newProgress = active.associate { item ->
                        item.id to downloadManager.queryProgress(item.downloadManagerId)
                    }
                    _progressMap.value = newProgress
                }
            }
        }
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch {
            downloadManager.deleteFile(item)
        }
    }

    fun cancelDownload(item: DownloadItem) {
        viewModelScope.launch {
            downloadManager.cancelDownload(item)
        }
    }
}
