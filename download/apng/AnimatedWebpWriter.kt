package com.CannedF0xy.hideout.download.apng

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile

class AnimatedWebpWriter(target: File, private val width: Int, private val height: Int, private val quality: Int) : Closeable {
    private val out = RandomAccessFile(target, "rw")
    private var frames = 0

    init {
        out.setLength(0)
        out.write("RIFF".toByteArray())
        out.write(le32(0))
        out.write("WEBP".toByteArray())
        val vp8x = ByteArray(10)
        vp8x[0] = (FLAG_ALPHA or FLAG_ANIMATION).toByte()
        put24(vp8x, 4, width - 1)
        put24(vp8x, 7, height - 1)
        writeChunk("VP8X", vp8x)
        val anim = ByteArray(6)
        writeChunk("ANIM", anim)
    }

    fun addFrame(bitmap: Bitmap, durationMs: Int) {
        val encoded = ByteArrayOutputStream()
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        if (!bitmap.compress(format, quality, encoded)) throw IllegalStateException("WebP encoding failed")
        val frameData = extractImageChunks(encoded.toByteArray())
        val header = ByteArray(16)
        put24(header, 0, 0)
        put24(header, 3, 0)
        put24(header, 6, bitmap.width - 1)
        put24(header, 9, bitmap.height - 1)
        put24(header, 12, durationMs.coerceIn(MIN_DURATION_MS, MAX_DURATION_MS))
        header[15] = FLAG_NO_BLEND.toByte()
        writeChunk("ANMF", header + frameData)
        frames++
    }

    override fun close() {
        try {
            if (frames > 0) {
                val size = out.length()
                out.seek(4)
                out.write(le32((size - 8).toInt()))
            }
        } finally {
            out.close()
        }
    }

    private fun writeChunk(fourCc: String, payload: ByteArray) {
        out.write(fourCc.toByteArray())
        out.write(le32(payload.size))
        out.write(payload)
        if (payload.size % 2 == 1) out.write(0)
    }

    private fun extractImageChunks(webp: ByteArray): ByteArray {
        if (webp.size < 12 || String(webp, 0, 4) != "RIFF" || String(webp, 8, 4) != "WEBP") {
            throw IllegalStateException("Unexpected WebP output")
        }
        val result = ByteArrayOutputStream()
        var offset = 12
        while (offset + 8 <= webp.size) {
            val fourCc = String(webp, offset, 4)
            val length = readLe32(webp, offset + 4)
            val padded = length + (length and 1)
            val end = (offset + 8 + padded).coerceAtMost(webp.size)
            if (fourCc == "ALPH" || fourCc == "VP8 " || fourCc == "VP8L") {
                result.write(webp, offset, end - offset)
            }
            offset += 8 + padded
        }
        if (result.size() == 0) throw IllegalStateException("WebP frame has no image data")
        return result.toByteArray()
    }

    private fun le32(value: Int) = byteArrayOf(value.toByte(), (value shr 8).toByte(), (value shr 16).toByte(), (value shr 24).toByte())

    private fun readLe32(data: ByteArray, at: Int): Int =
        (data[at].toInt() and 0xFF) or ((data[at + 1].toInt() and 0xFF) shl 8) or
            ((data[at + 2].toInt() and 0xFF) shl 16) or ((data[at + 3].toInt() and 0xFF) shl 24)

    private fun put24(target: ByteArray, at: Int, value: Int) {
        target[at] = value.toByte()
        target[at + 1] = (value shr 8).toByte()
        target[at + 2] = (value shr 16).toByte()
    }

    companion object {
        private const val FLAG_ALPHA = 0x10
        private const val FLAG_ANIMATION = 0x02
        private const val FLAG_NO_BLEND = 0x02
        private const val MIN_DURATION_MS = 10
        private const val MAX_DURATION_MS = 0xFFFFFF
    }
}
