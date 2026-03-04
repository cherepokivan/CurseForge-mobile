package com.curseforge.mobile.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.curseforge.mobile.domain.ServiceLocator
import com.curseforge.mobile.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId <= 0) return

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = context.getSharedPreferences("downloads", Context.MODE_PRIVATE)
            val fileName = prefs.getString("file_$downloadId", null) ?: return@launch
            val autoOpen = ServiceLocator.repository(context).settings().first().autoOpenAfterDownload
            if (!autoOpen) return@launch

            val file = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), fileName)
            if (!file.exists()) {
                Logger.log("Download", "File not found for id=$downloadId")
                return@launch
            }
            if (!fileName.endsWith(".mcpack") && !fileName.endsWith(".mcaddon")) {
                Logger.log("Download", "Unexpected extension for $fileName")
                return@launch
            }

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Logger.log("Download", "Opening downloaded pack $fileName")
            context.startActivity(openIntent)
        }
    }
}
