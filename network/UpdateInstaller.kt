package com.CannedF0xy.hideout.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object UpdateInstaller {
    private const val DIR_NAME = "updates"
    private const val FILE_NAME = "hideout-update.apk"
    private const val BUFFER_SIZE = 64 * 1024

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun download(context: Context, url: String, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, DIR_NAME).apply { if (!exists()) mkdirs() }
        val target = File(dir, FILE_NAME)
        if (target.exists()) target.delete()
        val request = Request.Builder().url(url).header("User-Agent", NetworkModule.DEFAULT_USER_AGENT).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val body = response.body ?: throw Exception("Empty response body")
            val total = body.contentLength()
            var written = 0L
            body.byteStream().use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        written += read
                        if (total > 0) {
                            val fraction = (written.toFloat() / total).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) { onProgress(fraction) }
                        }
                    }
                }
            }
        }
        target
    }

    fun canInstall(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()
    }

    fun openInstallPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
