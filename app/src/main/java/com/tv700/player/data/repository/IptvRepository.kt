package com.iptv.player.data.repository

import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.m3u.M3uParser
import com.iptv.player.data.model.XtreamStream
import com.iptv.player.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IptvRepository @Inject constructor(
    private val xtreamApi: XtreamApiService,
    private val m3uParser: M3uParser,
    private val playlistStore: PlaylistDataStore
) {

    // ── Xtream: Live channels ─────────────────────────────────────────────

    fun getLiveChannels(playlist: Playlist): Flow<Resource<List<Channel>>> = flow {
        emit(Resource.Loading)
        try {
            val streams = xtreamApi.getLiveStreams(playlist.username, playlist.password)
            emit(Resource.Success(streams.map { it.toLiveChannel(playlist) }))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load channels", e))
        }
    }

    // ── Xtream: VOD ───────────────────────────────────────────────────────

    fun getVodMovies(playlist: Playlist): Flow<Resource<List<VodMovie>>> = flow {
        emit(Resource.Loading)
        try {
            val streams = xtreamApi.getVodStreams(playlist.username, playlist.password)
            emit(Resource.Success(streams.map { it.toVodMovie(playlist) }))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load VOD", e))
        }
    }

    fun getVodDetail(playlist: Playlist, vodId: String): Flow<Resource<VodMovie>> = flow {
        emit(Resource.Loading)
        try {
            val info = xtreamApi.getVodInfo(playlist.username, playlist.password, vodId)
            val stream = info.movieData ?: throw Exception("VOD not found")
            val movie = stream.toVodMovie(playlist).copy(
                plot = info.info?.plot ?: "",
                director = info.info?.director ?: "",
                actors = info.info?.actors ?: "",
                backdropUrl = info.info?.backdrop_path?.firstOrNull() ?: "",
                rating = info.info?.rating ?: 0.0,
                releaseDate = info.info?.releasedate ?: "",
                duration = info.info?.duration ?: ""
            )
            emit(Resource.Success(movie))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load detail", e))
        }
    }

    // ── M3U ───────────────────────────────────────────────────────────────

    fun getM3uChannels(playlist: Playlist): Flow<Resource<List<Channel>>> = flow {
        emit(Resource.Loading)
        try {
            val channels = m3uParser.parseFromUrl(playlist.m3uUrl)
            emit(Resource.Success(channels))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to parse M3U", e))
        }
    }

    // ── Stream URL builders ───────────────────────────────────────────────

    private fun XtreamStream.toLiveChannel(playlist: Playlist): Channel {
        val url = "${playlist.serverUrl}/live/${playlist.username}/${playlist.password}/$streamId.m3u8"
        return Channel(
            id = streamId.toString(),
            name = name,
            streamUrl = url,
            logoUrl = streamIcon ?: "",
            group = categoryId ?: "Uncategorized",
            channelNumber = num,
            streamType = StreamType.LIVE,
            epgChannelId = epgChannelId ?: ""
        )
    }

    private fun XtreamStream.toVodMovie(playlist: Playlist): VodMovie {
        val ext = containerExtension ?: "mp4"
        val url = "${playlist.serverUrl}/movie/${playlist.username}/${playlist.password}/$streamId.$ext"
        return VodMovie(
            id = streamId.toString(),
            name = name,
            streamUrl = url,
            posterUrl = streamIcon ?: "",
            backdropUrl = "",
            plot = "",
            genre = "",
            rating = rating5 ?: 0.0,
            duration = "",
            releaseDate = added ?: "",
            director = "",
            actors = "",
            categoryId = categoryId ?: ""
        )
    }
}
