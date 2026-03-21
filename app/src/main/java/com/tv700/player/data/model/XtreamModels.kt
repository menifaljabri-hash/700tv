package com.iptv.player.data.model

import com.google.gson.annotations.SerializedName

data class XtreamServerInfo(
    @SerializedName("user_info") val userInfo: XtreamUserInfo,
    @SerializedName("server_info") val serverInfo: XtreamServer
)

data class XtreamUserInfo(
    val username: String,
    val password: String,
    val status: String,                     // "Active" | "Disabled" | "Expired"
    @SerializedName("exp_date") val expDate: Long?,
    @SerializedName("is_trial") val isTrial: String,
    @SerializedName("active_cons") val activeCons: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("max_connections") val maxConnections: String,
    @SerializedName("allowed_output_formats") val allowedFormats: List<String>
)

data class XtreamServer(
    val url: String,
    val port: String,
    @SerializedName("https_port") val httpsPort: String?,
    val protocol: String,
    val timezone: String,
    @SerializedName("timestamp_now") val timestampNow: Long,
    @SerializedName("time_now") val timeNow: String
)

data class XtreamCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int
)

data class XtreamStream(
    val num: Int,
    val name: String,
    @SerializedName("stream_type") val streamType: String,   // live | movie | series
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val archiveDuration: Int,
    val rating: String?,
    @SerializedName("rating_5based") val rating5: Double?,
    @SerializedName("container_extension") val containerExtension: String?
)

data class XtreamVodInfo(
    val info: VodDetails?,
    @SerializedName("movie_data") val movieData: XtreamStream?
)

data class VodDetails(
    val kinopoisk_url: String?,
    val tmdb_id: String?,
    val name: String?,
    val o_name: String?,
    val cover_big: String?,
    val movie_image: String?,
    val releasedate: String?,
    val episode_run_time: String?,
    val youtube_trailer: String?,
    val director: String?,
    val actors: String?,
    val cast: String?,
    val description: String?,
    val plot: String?,
    val age: String?,
    val mpaa_rating: String?,
    val rating_count_kinopoisk: Int?,
    val country: String?,
    val genre: String?,
    val backdrop_path: List<String>?,
    val duration_secs: Int?,
    val duration: String?,
    val video: Map<String, Any>?,
    val audio: Map<String, Any>?,
    val bitrate: Int?,
    val rating: Double?
)
