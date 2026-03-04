package com.curseforge.mobile.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.curseforge.mobile.util.Logger

class DownloadHelper(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueueDownload(fileId: Long, fileName: String, url: String): Long {
        val safeName = ensureSuffix(fileName)
        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(safeName)
            .setDescription("Загрузка аддона")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            .setMimeType("application/octet-stream")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val id = downloadManager.enqueue(request)
        context.getSharedPreferences("downloads", Context.MODE_PRIVATE)
            .edit()
            .putString("file_$id", safeName)
            .putLong("addon_file_id_$id", fileId)
            .apply()
        Logger.log("Download", "Enqueued fileId=$fileId requestId=$id")
        return id
    }

    private fun ensureSuffix(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.endsWith(".mcpack") || lower.endsWith(".mcaddon") -> name
            else -> "$name.mcpack"
        }
    }
}
