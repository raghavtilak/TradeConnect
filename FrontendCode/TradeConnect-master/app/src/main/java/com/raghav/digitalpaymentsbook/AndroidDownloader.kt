package com.raghav.digitalpaymentsbook


import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class AndroidDownloader(
     context: Context
) {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    fun downloadFile(url: String,token: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("TradeConnectSampleFormat.xlsx")
            .addRequestHeader("Authorization", "Bearer $token")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "TradeConnectSampleFormat.xlsx")
        return downloadManager.enqueue(request)
    }
}