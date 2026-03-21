package com.tv700.player.data.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.tv700.player.R

/**
 * Receives Android DownloadManager's DOWNLOAD_COMPLETE broadcast.
 * Shows a toast and can trigger a local notification or update Room DB.
 */
class DownloadCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))

        cursor.use {
            if (!it.moveToFirst()) return

            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val title  = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                ?.removePrefix("700TV — ") ?: "Video"

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    Toast.makeText(
                        context,
                        "✅ $title — ${context.getString(R.string.download_complete)}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                DownloadManager.STATUS_FAILED -> {
                    Toast.makeText(
                        context,
                        "❌ $title — ${context.getString(R.string.download_failed)}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
