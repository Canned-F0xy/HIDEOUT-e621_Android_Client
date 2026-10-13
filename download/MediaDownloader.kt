package com.CannedF0xy.hideout.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.widget.Toast
import com.CannedF0xy.hideout.i18n.AppStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.util.getCachedFile
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun saveFileToStorage(context: Context, sourceFile: File, fileName: String, treeUriStr: String) {
    if (treeUriStr == VaultStore.TARGET) {
        VaultStore.save(context, sourceFile, fileName)
        return
    }
    if (treeUriStr.isEmpty()) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val ext = sourceFile.extension
            val mimeType = when {
                ext.equals("webm", true) || ext.equals("mp4", true) -> "video/$ext"
                ext.equals("jpg", true) || ext.equals("jpeg", true) -> "image/jpeg"
                else -> "image/$ext"
            }
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/HIDEOUT")
            }
            val table = if (mimeType.startsWith("video")) android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI else android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uri = context.contentResolver.insert(table, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { it.copyTo(out) }
                }
            } else {
                throw Exception("Failed to save to MediaStore")
            }
        } else {
            val targetDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
            val hideoutDir = File(targetDir, "HIDEOUT")
            if (!hideoutDir.exists()) hideoutDir.mkdirs()
            val nomediaFile = File(hideoutDir, ".nomedia")
            if (!nomediaFile.exists()) nomediaFile.createNewFile()

            val destFile = File(hideoutDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            scanIntent.data = Uri.fromFile(destFile)
            context.sendBroadcast(scanIntent)
        }
    } else {
        val treeUri = Uri.parse(treeUriStr)
        val documentId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
        val docUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        val newDocUri = android.provider.DocumentsContract.createDocument(context.contentResolver, docUri, "*/*", fileName)
        if (newDocUri != null) {
            context.contentResolver.openOutputStream(newDocUri)?.use { out ->
                sourceFile.inputStream().use { it.copyTo(out) }
            }
        } else {
            throw Exception("Failed to save to selected folder")
        }
    }
}

fun downloadMediaWithRange(
    context: Context,
    url: String,
    fileName: String,
    md5: String?,
    ext: String,
    treeUriStr: String,
    cfClearanceCookie: String,
    strings: AppStrings
) {
    if (!md5.isNullOrBlank()) {
        val cached = getCachedFile(context, md5, ext)
        if (cached != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    saveFileToStorage(context, cached, fileName, treeUriStr)
                    withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadComplete, Toast.LENGTH_SHORT).show() }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadFailed(e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show() }
                }
            }
            return
        }
    }

    Toast.makeText(context, strings.downloadStarting, Toast.LENGTH_SHORT).show()
    DownloadQueue.enqueue(
        context = context,
        request = DownloadRequest(url = url, fileName = fileName, md5 = md5, ext = ext),
        treeUri = treeUriStr,
        strings = strings,
        silent = false
    )
}
