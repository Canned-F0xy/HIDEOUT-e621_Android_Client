package com.CannedF0xy.hideout.download

import android.content.Context
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VaultStore {
    const val TARGET = "hideout-vault:"
    private const val DIR_NAME = "vault"

    private val _files = MutableStateFlow<List<File>>(emptyList())
    val files: StateFlow<List<File>> = _files.asStateFlow()

    private fun dir(context: Context): File = File(context.applicationContext.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    fun isVideo(file: File): Boolean {
        val ext = file.extension.lowercase()
        return ext == "webm" || ext == "mp4"
    }

    fun refresh(context: Context) {
        _files.value = dir(context).listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun save(context: Context, source: File, fileName: String) {
        val folder = dir(context)
        var target = File(folder, fileName)
        if (target.exists()) {
            val base = fileName.substringBeforeLast(".")
            val ext = fileName.substringAfterLast(".", "")
            var index = 1
            while (target.exists()) {
                target = File(folder, if (ext.isEmpty()) "${base}_$index" else "${base}_$index.$ext")
                index++
            }
        }
        source.copyTo(target)
        refresh(context)
    }

    fun delete(context: Context, file: File) {
        if (file.parentFile?.absolutePath == dir(context).absolutePath) file.delete()
        refresh(context)
    }

    fun exportToGallery(context: Context, file: File) {
        saveFileToStorage(context, file, file.name, "")
    }
}
