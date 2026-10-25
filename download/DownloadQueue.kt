package com.CannedF0xy.hideout.download

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.CannedF0xy.hideout.i18n.AppStrings
import com.CannedF0xy.hideout.i18n.KoStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.notification.NotificationHelper
import com.CannedF0xy.hideout.util.getCachedFile
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DownloadStatus { QUEUED, RUNNING, DONE, FAILED, CANCELED }

data class DownloadRequest(
    val url: String,
    val fileName: String,
    val md5: String?,
    val ext: String
)

data class DownloadTask(
    val id: Long,
    val request: DownloadRequest,
    val treeUri: String,
    val silent: Boolean,
    val progress: Float = 0f,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val error: String? = null
)

object DownloadQueue {
    private const val NOTIFICATION_ID = 7101
    private const val BUFFER_SIZE = 64 * 1024
    private const val PROGRESS_INTERVAL_MS = 300L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private val idGenerator = AtomicLong(0)
    private val canceledIds: MutableSet<Long> = Collections.newSetFromMap(ConcurrentHashMap<Long, Boolean>())
    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    private var runner: Job? = null
    private var appContext: Context? = null
    private var strings: AppStrings = KoStrings
    private var lastNotifiedPercent = -1
    private var lastProgressUpdateAt = 0L

    fun enqueue(context: Context, request: DownloadRequest, treeUri: String, strings: AppStrings, silent: Boolean): Long {
        return enqueueAll(context, listOf(request), treeUri, strings, silent).first()
    }

    fun enqueueAll(context: Context, requests: List<DownloadRequest>, treeUri: String, strings: AppStrings, silent: Boolean): List<Long> {
        appContext = context.applicationContext
        this.strings = strings
        val newTasks = requests.map { DownloadTask(idGenerator.incrementAndGet(), it, treeUri, silent) }
        _tasks.update { it + newTasks }
        ensureRunner()
        return newTasks.map { it.id }
    }

    fun cancel(id: Long) {
        canceledIds.add(id)
        _tasks.update { list ->
            list.map { if (it.id == id && it.status == DownloadStatus.QUEUED) it.copy(status = DownloadStatus.CANCELED) else it }
        }
    }

    fun cancelAll() {
        _tasks.value.filter { it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.RUNNING }.forEach { cancel(it.id) }
    }

    fun clearFinished() {
        _tasks.update { list -> list.filter { it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.RUNNING } }
    }

    fun retry(id: Long) {
        canceledIds.remove(id)
        _tasks.update { list ->
            list.map {
                if (it.id == id && (it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELED)) {
                    it.copy(status = DownloadStatus.QUEUED, progress = 0f, error = null)
                } else it
            }
        }
        ensureRunner()
    }

    private fun ensureRunner() {
        synchronized(lock) {
            if (runner == null) runner = scope.launch { processLoop() }
        }
    }

    private suspend fun processLoop() {
        while (true) {
            val next = synchronized(lock) {
                val candidate = _tasks.value.firstOrNull { it.status == DownloadStatus.QUEUED }
                if (candidate == null) runner = null
                candidate
            } ?: break
            process(next)
        }
        showSummaryNotification()
    }

    private fun updateTask(id: Long, transform: (DownloadTask) -> DownloadTask) {
        _tasks.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    private suspend fun process(task: DownloadTask) {
        val context = appContext ?: return
        val request = task.request
        updateTask(task.id) { it.copy(status = DownloadStatus.RUNNING, progress = 0f) }
        lastNotifiedPercent = -1
        showProgressNotification(context, request.fileName, 0)

        try {
            val cached = getCachedFile(context, request.md5, request.ext)
            if (cached != null) {
                saveFileToStorage(context, cached, request.fileName, task.treeUri)
            } else {
                downloadToStorage(context, task)
            }
            updateTask(task.id) { it.copy(status = DownloadStatus.DONE, progress = 1f) }
            if (!task.silent) toast(context, strings.downloadComplete)
        } catch (e: DownloadCanceledException) {
            updateTask(task.id) { it.copy(status = DownloadStatus.CANCELED) }
        } catch (e: Exception) {
            val message = e.localizedMessage ?: ""
            updateTask(task.id) { it.copy(status = DownloadStatus.FAILED, error = message) }
            if (!task.silent) toast(context, strings.downloadFailed(message))
        }
    }

    private fun downloadToStorage(context: Context, task: DownloadTask) {
        val request = task.request
        val tempKey = request.md5?.takeIf { it.isNotBlank() } ?: request.fileName
        val tempFile = File(context.cacheDir, "temp_${tempKey}.${request.ext}")
        var downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L

        val requestBuilder = okhttp3.Request.Builder().url(request.url)
        if (downloadedBytes > 0) {
            requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
        }

        NetworkModule.client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val isPartial = response.code == 206
            if (!isPartial && downloadedBytes > 0) {
                downloadedBytes = 0L
                tempFile.delete()
            }

            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            val totalBytes = if (contentLength > 0) contentLength + downloadedBytes else -1L
            var written = downloadedBytes

            body.byteStream().use { input ->
                FileOutputStream(tempFile, downloadedBytes > 0).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        if (canceledIds.contains(task.id)) {
                            throw DownloadCanceledException()
                        }
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        written += read
                        if (totalBytes > 0) {
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastProgressUpdateAt >= PROGRESS_INTERVAL_MS) {
                                lastProgressUpdateAt = now
                                val fraction = (written.toFloat() / totalBytes).coerceIn(0f, 1f)
                                updateTask(task.id) { it.copy(progress = fraction) }
                                showProgressNotification(context, request.fileName, (fraction * 100).toInt())
                            }
                        }
                    }
                }
            }
        }

        saveFileToStorage(context, tempFile, request.fileName, task.treeUri)

        if (!request.md5.isNullOrBlank()) {
            val cacheDir = File(context.cacheDir, "e621_media_cache")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            tempFile.copyTo(File(cacheDir, "${request.md5}.${request.ext}"), overwrite = true)
        }
        tempFile.delete()
    }

    private fun showProgressNotification(context: Context, fileName: String, percent: Int) {
        if (percent == lastNotifiedPercent) return
        lastNotifiedPercent = percent
        val snapshot = _tasks.value
        val finished = snapshot.count { it.status == DownloadStatus.DONE || it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELED }
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(strings.downloadNotificationTitle(finished, snapshot.size))
            .setContentText(fileName)
            .setProgress(100, percent, percent <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(NotificationHelper.openAppIntent(context, NOTIFICATION_ID))
            .build()
        NotificationHelper.notify(context, NOTIFICATION_ID, notification)
    }

    private fun showSummaryNotification() {
        val context = appContext ?: return
        val snapshot = _tasks.value
        val done = snapshot.count { it.status == DownloadStatus.DONE }
        val failed = snapshot.count { it.status == DownloadStatus.FAILED }
        if (done + failed == 0) {
            NotificationHelper.cancel(context, NOTIFICATION_ID)
            return
        }
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(strings.downloadSummaryTitle)
            .setContentText(strings.downloadSummary(done, failed))
            .setAutoCancel(true)
            .setContentIntent(NotificationHelper.openAppIntent(context, NOTIFICATION_ID))
            .build()
        NotificationHelper.notify(context, NOTIFICATION_ID, notification)
    }

    private suspend fun toast(context: Context, message: String) {
        withContext(Dispatchers.Main) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    private class DownloadCanceledException : Exception()
}
