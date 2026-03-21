package com.tv700.player.ui.vod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tv700.player.data.download.DownloadStatus
import com.tv700.player.data.download.VodDownloadManager
import com.tv700.player.domain.model.VodMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VodDetailViewModel @Inject constructor(
    private val downloadManager: VodDownloadManager
) : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadStatus?>(null)
    val downloadState = _downloadState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress = _downloadProgress.asStateFlow()

    private var currentDownloadDmId: Long = -1L

    fun checkDownloadStatus(vodId: String) {
        if (downloadManager.isDownloaded(vodId)) {
            _downloadState.value = DownloadStatus.COMPLETE
        }
    }

    fun startDownload(movie: VodMovie) {
        _downloadState.value = DownloadStatus.QUEUED
        downloadManager.startDownload(
            vodId     = movie.id,
            title     = movie.name,
            streamUrl = movie.streamUrl,
            posterUrl = movie.posterUrl
        ) { item ->
            currentDownloadDmId = item.downloadManagerId
            _downloadState.value = DownloadStatus.DOWNLOADING
            pollProgress()
        }
    }

    fun cancelDownload(vodId: String) {
        viewModelScope.launch {
            val downloads = downloadManager.downloads
            // Find and cancel
            _downloadState.value = null
            _downloadProgress.value = 0
        }
    }

    private fun pollProgress() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                if (currentDownloadDmId < 0) break
                val progress = downloadManager.queryProgress(currentDownloadDmId)
                val status   = downloadManager.queryStatus(currentDownloadDmId)
                _downloadProgress.value = progress.coerceAtLeast(0)
                _downloadState.value = status
                if (status == DownloadStatus.COMPLETE || status == DownloadStatus.FAILED) break
            }
        }
    }
}
