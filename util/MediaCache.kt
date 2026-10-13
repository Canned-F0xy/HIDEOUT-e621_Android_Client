package com.CannedF0xy.hideout.util

import android.content.Context
import androidx.paging.filter
import java.io.File

fun getCachedFile(context: Context, md5: String?, ext: String?): File? {
    if (md5.isNullOrBlank() || ext.isNullOrBlank()) return null
    val cacheDir = File(context.cacheDir, "e621_media_cache")
    if (!cacheDir.exists()) return null
    val file = File(cacheDir, "$md5.$ext")
    return if (file.exists() && file.length() > 0) file else null
}

fun getCacheDirSize(context: Context): Long {
    val cacheDir = context.cacheDir
    if (!cacheDir.exists()) return 0L
    return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

fun getCacheSizeString(context: Context): String {
    return formatByteSize(getCacheDirSize(context))
}
