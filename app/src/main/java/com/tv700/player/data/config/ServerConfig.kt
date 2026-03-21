package com.tv700.player.data.config

/**
 * 700TV — Pre-configured DNS / Server settings.
 * The default server is http://taame.live:80
 * Users can override by adding their own playlist,
 * but this is the out-of-box DNS entry point.
 */
object ServerConfig {

    // ── Default DNS server ────────────────────────────────────────────────
    const val DEFAULT_SERVER_URL  = "http://taame.live:80"
    const val DEFAULT_SERVER_HOST = "taame.live"
    const val DEFAULT_SERVER_PORT = 80

    // ── App identity ──────────────────────────────────────────────────────
    const val APP_NAME       = "700TV"
    const val APP_VERSION    = "1.0.0"
    const val USER_AGENT     = "700TV-Android/$APP_VERSION"

    // ── Stream URL builders ───────────────────────────────────────────────

    fun buildLiveUrl(username: String, password: String, streamId: Int, server: String = DEFAULT_SERVER_URL): String =
        "$server/live/$username/$password/$streamId.m3u8"

    fun buildVodUrl(username: String, password: String, streamId: Int, ext: String = "mp4", server: String = DEFAULT_SERVER_URL): String =
        "$server/movie/$username/$password/$streamId.$ext"

    fun buildSeriesUrl(username: String, password: String, streamId: Int, ext: String = "mp4", server: String = DEFAULT_SERVER_URL): String =
        "$server/series/$username/$password/$streamId.$ext"

    fun buildApiUrl(server: String = DEFAULT_SERVER_URL): String =
        "$server/player_api.php"

    // ── Pre-built default Playlist object ────────────────────────────────
    // Shown in Add Playlist screen as "Default Server" option.
    // User fills in their username + password, server URL is pre-filled.
    val DEFAULT_PLAYLIST_TEMPLATE = mapOf(
        "serverUrl"   to DEFAULT_SERVER_URL,
        "displayName" to "700TV — taame.live",
        "protocol"    to "http",
        "host"        to DEFAULT_SERVER_HOST,
        "port"        to DEFAULT_SERVER_PORT.toString()
    )
}
