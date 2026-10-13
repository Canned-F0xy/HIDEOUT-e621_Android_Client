package com.CannedF0xy.hideout.util

import java.util.Locale

fun formatDuration(seconds: Double?): String {
    if (seconds == null || seconds <= 0) return ""
    val totalSeconds = seconds.toInt()
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}

fun formatByteSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.2f KB", kb)
        else -> "$bytes Bytes"
    }
}
