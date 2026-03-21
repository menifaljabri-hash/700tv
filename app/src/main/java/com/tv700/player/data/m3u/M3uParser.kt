package com.tv700.player.data.m3u

import com.tv700.player.domain.model.Channel
import com.tv700.player.domain.model.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uParser @Inject constructor() {

    companion object {
        private const val EXTINF = "#EXTINF:"
        private const val EXT_X_STREAM_INF = "#EXT-X-STREAM-INF"
        private val TVG_ID_REGEX = Regex("""tvg-id="([^"]*?)"""")
        private val TVG_NAME_REGEX = Regex("""tvg-name="([^"]*?)"""")
        private val TVG_LOGO_REGEX = Regex("""tvg-logo="([^"]*?)"""")
        private val GROUP_TITLE_REGEX = Regex("""group-title="([^"]*?)"""")
        private val TVG_CHNO_REGEX = Regex("""tvg-chno="([^"]*?)"""")
    }

    /**
     * Parse an M3U playlist from a remote URL.
     */
    suspend fun parseFromUrl(m3uUrl: String): List<Channel> = withContext(Dispatchers.IO) {
        val conn = URL(m3uUrl).openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "IPTV-Player/1.0")
        }
        try {
            conn.inputStream.use { parse(it) }
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Parse an M3U playlist from a local InputStream.
     */
    suspend fun parse(inputStream: InputStream): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        var currentMeta: String? = null
        var lineNumber = 0

        reader.forEachLine { rawLine ->
            lineNumber++
            val line = rawLine.trim()
            when {
                line.startsWith(EXTINF) -> {
                    currentMeta = line
                }
                line.startsWith("#") -> {
                    // Skip other directives
                }
                line.isNotEmpty() && currentMeta != null -> {
                    val channel = buildChannel(currentMeta!!, line, lineNumber)
                    channels.add(channel)
                    currentMeta = null
                }
            }
        }

        channels
    }

    private fun buildChannel(meta: String, url: String, index: Int): Channel {
        val tvgId    = TVG_ID_REGEX.find(meta)?.groupValues?.get(1) ?: ""
        val tvgName  = TVG_NAME_REGEX.find(meta)?.groupValues?.get(1) ?: ""
        val logo     = TVG_LOGO_REGEX.find(meta)?.groupValues?.get(1) ?: ""
        val group    = GROUP_TITLE_REGEX.find(meta)?.groupValues?.get(1) ?: "Uncategorized"
        val chNo     = TVG_CHNO_REGEX.find(meta)?.groupValues?.get(1) ?: index.toString()

        // Fallback name: parse after the last comma in EXTINF line
        val fallbackName = meta.substringAfterLast(",").trim()
        val displayName = tvgName.ifEmpty { fallbackName }.ifEmpty { "Channel $index" }

        val streamType = when {
            url.contains("/movie/") -> StreamType.VOD
            url.contains("/series/") -> StreamType.SERIES
            else -> StreamType.LIVE
        }

        return Channel(
            id = tvgId.ifEmpty { "m3u_$index" },
            name = displayName,
            streamUrl = url,
            logoUrl = logo,
            group = group,
            channelNumber = chNo.toIntOrNull() ?: index,
            streamType = streamType,
            epgChannelId = tvgId
        )
    }
}
