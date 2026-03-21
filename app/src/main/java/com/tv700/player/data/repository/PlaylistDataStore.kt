package com.tv700.player.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.tv700.player.domain.model.Playlist
import com.tv700.player.domain.model.PlaylistType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playlistDataStore by preferencesDataStore("playlist_store")

@Singleton
class PlaylistDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val KEY_PLAYLISTS = stringPreferencesKey("playlists_json")
    private val KEY_ACTIVE_ID = stringPreferencesKey("active_playlist_id")

    suspend fun savePlaylists(playlists: List<Playlist>) {
        context.playlistDataStore.edit { prefs ->
            prefs[KEY_PLAYLISTS] = gson.toJson(playlists)
        }
    }

    suspend fun getPlaylists(): List<Playlist> {
        val json = context.playlistDataStore.data.map { it[KEY_PLAYLISTS] }.first()
        return if (json.isNullOrBlank()) emptyList()
        else gson.fromJson(json, Array<Playlist>::class.java).toList()
    }

    suspend fun addPlaylist(playlist: Playlist) {
        val current = getPlaylists().toMutableList()
        current.removeIf { it.id == playlist.id }
        current.add(playlist)
        savePlaylists(current)
    }

    suspend fun setActivePlaylist(id: String) {
        context.playlistDataStore.edit { it[KEY_ACTIVE_ID] = id }
    }

    suspend fun getActivePlaylist(): Playlist? {
        val activeId = context.playlistDataStore.data.map { it[KEY_ACTIVE_ID] }.first()
        return getPlaylists().firstOrNull { it.id == activeId } ?: getPlaylists().firstOrNull()
    }

    suspend fun deletePlaylist(id: String) {
        val updated = getPlaylists().filter { it.id != id }
        savePlaylists(updated)
    }
}
