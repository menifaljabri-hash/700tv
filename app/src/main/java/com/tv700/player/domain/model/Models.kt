package com.iptv.player.domain.model

enum class StreamType { LIVE, VOD, SERIES }

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String,
    val group: String,
    val channelNumber: Int,
    val streamType: StreamType,
    val epgChannelId: String = "",
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false      // parental lock per-channel
)

data class VodMovie(
    val id: String,
    val name: String,
    val streamUrl: String,
    val posterUrl: String,
    val backdropUrl: String,
    val plot: String,
    val genre: String,
    val rating: Double,
    val duration: String,
    val releaseDate: String,
    val director: String,
    val actors: String,
    val categoryId: String,
    val isLocked: Boolean = false
)

data class Playlist(
    val id: String,
    val name: String,
    val type: PlaylistType,
    // Xtream Codes fields
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    // M3U fields
    val m3uUrl: String = "",
    val isActive: Boolean = true
)

enum class PlaylistType { XTREAM, M3U }

data class Category(
    val id: String,
    val name: String,
    val streamType: StreamType
)

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
