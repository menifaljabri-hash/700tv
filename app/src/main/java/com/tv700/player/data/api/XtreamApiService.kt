package com.tv700.player.data.api

import com.tv700.player.data.model.XtreamCategory
import com.tv700.player.data.model.XtreamServerInfo
import com.tv700.player.data.model.XtreamStream
import com.tv700.player.data.model.XtreamVodInfo
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Xtream Codes API v2 — full surface coverage.
 * Base URL must end with a slash: "http://server:port/"
 * All calls require username + password query params.
 */
interface XtreamApiService {

    // ── Auth & server info ────────────────────────────────────────────────

    @GET("player_api.php")
    suspend fun getServerInfo(
        @Query("username") username: String,
        @Query("password") password: String
    ): XtreamServerInfo

    // ── Live TV ───────────────────────────────────────────────────────────

    @GET("player_api.php?action=get_live_categories")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategory>

    @GET("player_api.php?action=get_live_streams")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamStream>

    // ── VOD ───────────────────────────────────────────────────────────────

    @GET("player_api.php?action=get_vod_categories")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategory>

    @GET("player_api.php?action=get_vod_streams")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamStream>

    @GET("player_api.php?action=get_vod_info")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("vod_id") vodId: String
    ): XtreamVodInfo

    // ── Series ────────────────────────────────────────────────────────────

    @GET("player_api.php?action=get_series_categories")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategory>

    @GET("player_api.php?action=get_series")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamStream>
}
