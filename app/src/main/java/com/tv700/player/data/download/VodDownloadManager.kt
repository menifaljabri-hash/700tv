package com.tv700.player.data.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tv700.player.data.config.ServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.downloadDataStore by preferencesDataStore("download_store")

data class DownloadItem(
    val id: String,                 // VOD stream ID
    val title: String,
    val posterUrl: String,
    val localPath: String,          // absolute file path when complete
    val downloadManagerId: Long,    // Android DownloadManager ID
    val status: DownloadStatus,
    val fileSizeBytes: Long = 0L,
    val addedAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, COMPLETE, FAILED, PAUSED
}

@Singleton
class VodDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val androidDM: DownloadManager = context.getSystemService()!!
    private val gson = Gson()
    private val KEY_DOWNLOADS = stringPreferencesKey("downloads_json")

    private val _activeDownloads = MutableStateFlow<Map<String, Int>>(emptyMap()) // id → progress 0-100
    val activeDownloads = _activeDownloads.asStateFlow()

    val downloads: Flow<List<DownloadItem>> = context.downloadDataStore.data.map { prefs ->
        val json = prefs[KEY_DOWNLOADS] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<DownloadItem>>() {}.type
            gson.fromJson<List<DownloadItem>>(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    /**
     * Start downloading a VOD file.
     * Uses Android DownloadManager — handles pause/resume, progress notification.
     */
    fun startDownload(
        vodId: String,
        title: String,
        streamUrl: String,
        posterUrl: String,
        onQueued: (DownloadItem) -> Unit
    ) {
        val safeTitle = title.replace("[^a-zA-Z0-9._\\- ]".toRegex(), "")
        val fileName  = "700TV_${safeTitle.take(40)}_$vodId.mp4"

        val request = DownloadManager.Request(Uri.parse(streamUrl)).apply {
            setTitle("700TV — $title")
            setDescription("Downloading…")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_MOVIES,
                "700TV/$fileName"
            )
            addRequestHeader("User-Agent", ServerConfig.USER_AGENT)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }

        val dmId = androidDM.enqueue(request)
        val localPath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "700TV/$fileName"
        ).absolutePath

        val item = DownloadItem(
            id                = vodId,
            title             = title,
            posterUrl         = posterUrl,
            localPath         = localPath,
            downloadManagerId = dmId,
            status            = DownloadStatus.QUEUED
        )
        onQueued(item)
        saveDownload(item)
    }

    /**
     * Query live progress (0–100) for a download.
     */
    fun queryProgress(downloadManagerId: Long): Int {
        val cursor = androidDM.query(DownloadManager.Query().setFilterById(downloadManagerId))
        cursor.use {
            if (!it.moveToFirst()) return -1
            val total    = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val status   = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

            return when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> 100
                DownloadManager.STATUS_FAILED     -> -1
                else -> if (total > 0) ((downloaded * 100) / total).toInt() else 0
            }
        }
    }

    fun queryStatus(downloadManagerId: Long): DownloadStatus {
        val cursor = androidDM.query(DownloadManager.Query().setFilterById(downloadManagerId))
        cursor.use {
            if (!it.moveToFirst()) return DownloadStatus.FAILED
            return when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETE
                DownloadManager.STATUS_FAILED     -> DownloadStatus.FAILED
                DownloadManager.STATUS_PAUSED     -> DownloadStatus.PAUSED
                DownloadManager.STATUS_RUNNING    -> DownloadStatus.DOWNLOADING
                else                              -> DownloadStatus.QUEUED
            }
        }
    }

    fun cancelDownload(item: DownloadItem) {
        androidDM.remove(item.downloadManagerId)
        deleteDownload(item)
    }

    fun deleteFile(item: DownloadItem) {
        File(item.localPath).takeIf { it.exists() }?.delete()
        deleteDownload(item)
    }

    fun isDownloaded(vodId: String): Boolean {
        // Synchronous check via shared prefs snapshot — used in UI
        val sharedPrefs = context.getSharedPreferences("dl_index", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(vodId, false)
    }

    private fun saveDownload(item: DownloadItem) {
        // Persist index for fast isDownloaded() checks
        context.getSharedPreferences("dl_index", Context.MODE_PRIVATE)
            .edit().putBoolean(item.id, true).apply()

        // Full data in DataStore (async OK — fire and forget)
        kotlinx.coroutines.GlobalScope.let {
            it.launch(kotlinx.coroutines.Dispatchers.IO) {
                context.downloadDataStore.edit { prefs ->
                    val current = try {
                        val json = prefs[KEY_DOWNLOADS] ?: "[]"
                        val type = object : TypeToken<MutableList<DownloadItem>>() {}.type
                        gson.fromJson<MutableList<DownloadItem>>(json, type) ?: mutableListOf()
                    } catch (e: Exception) { mutableListOf() }
                    current.removeIf { d -> d.id == item.id }
                    current.add(0, item)
                    prefs[KEY_DOWNLOADS] = gson.toJson(current)
                }
            }
        }
    }

    private fun deleteDownload(item: DownloadItem) {
        context.getSharedPreferences("dl_index", Context.MODE_PRIVATE)
            .edit().remove(item.id).apply()
        kotlinx.coroutines.GlobalScope.let {
            it.launch(kotlinx.coroutines.Dispatchers.IO) {
                context.downloadDataStore.edit { prefs ->
                    val current = try {
                        val json = prefs[KEY_DOWNLOADS] ?: "[]"
                        val type = object : TypeToken<MutableList<DownloadItem>>() {}.type
                        gson.fromJson<MutableList<DownloadItem>>(json, type) ?: mutableListOf()
                    } catch (e: Exception) { mutableListOf() }
                    current.removeIf { d -> d.id == item.id }
                    prefs[KEY_DOWNLOADS] = gson.toJson(current)
                }
            }
        }
    }
}
