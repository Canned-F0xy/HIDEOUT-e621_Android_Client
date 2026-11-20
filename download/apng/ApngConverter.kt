package com.CannedF0xy.hideout.download.apng

import java.io.File
import java.io.RandomAccessFile

enum class ApngExportFormat(val key: String, val extension: String?) {
    WEBP("webp", "webp"),
    GIF("gif", "gif"),
    ORIGINAL("original", null);

    companion object {
        val DEFAULT = WEBP
        fun fromKey(key: String?): ApngExportFormat = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

data class ConvertedFile(val file: File, val fileName: String)

object ApngConverter {
    private const val WEBP_QUALITY = 90
    private const val MAX_GIF_DIMENSION = 1280
    private const val MAX_CHUNKS_TO_SCAN = 64
    private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    fun isApng(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        return try {
            RandomAccessFile(file, "r").use { input ->
                val signature = ByteArray(8)
                input.readFully(signature)
                if (!signature.contentEquals(PNG_SIGNATURE)) return false
                repeat(MAX_CHUNKS_TO_SCAN) {
                    if (input.filePointer + 8 > input.length()) return false
                    val length = input.readInt().toLong() and 0xFFFFFFFFL
                    val type = ByteArray(4)
                    input.readFully(type)
                    when (String(type, Charsets.US_ASCII)) {
                        "acTL" -> return true
                        "IDAT" -> return false
                    }
                    input.seek(input.filePointer + length + 4)
                }
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun convertIfNeeded(source: File, fileName: String, format: ApngExportFormat, workDir: File): ConvertedFile? {
        val extension = format.extension ?: return null
        if (!fileName.endsWith(".png", ignoreCase = true) && !source.name.endsWith(".png", ignoreCase = true)) return null
        if (!isApng(source)) return null
        val baseName = fileName.substringBeforeLast('.', fileName)
        val outputName = "$baseName.$extension"
        if (!workDir.exists()) workDir.mkdirs()
        val output = File(workDir, "convert_${System.nanoTime()}.$extension")
        var webpWriter: AnimatedWebpWriter? = null
        var gifWriter: GifWriter? = null
        var frameCount = 0
        return try {
            ApngFrameExtractor.extract(source) { bitmap, duration ->
                when (format) {
                    ApngExportFormat.WEBP -> {
                        val writer = webpWriter ?: AnimatedWebpWriter(output, bitmap.width, bitmap.height, WEBP_QUALITY).also { webpWriter = it }
                        writer.addFrame(bitmap, duration)
                    }
                    ApngExportFormat.GIF -> {
                        val writer = gifWriter ?: run {
                            val scale = minOf(1f, MAX_GIF_DIMENSION.toFloat() / maxOf(bitmap.width, bitmap.height))
                            val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
                            val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
                            GifWriter(output, width, height).also { gifWriter = it }
                        }
                        writer.addFrame(bitmap, duration)
                    }
                    ApngExportFormat.ORIGINAL -> Unit
                }
                frameCount++
            }
            webpWriter?.close()
            gifWriter?.close()
            webpWriter = null
            gifWriter = null
            if (frameCount > 0 && output.length() > 0) {
                ConvertedFile(output, outputName)
            } else {
                output.delete()
                null
            }
        } catch (e: Throwable) {
            try {
                webpWriter?.close()
                gifWriter?.close()
            } catch (ignored: Throwable) {
            }
            output.delete()
            null
        }
    }
}
