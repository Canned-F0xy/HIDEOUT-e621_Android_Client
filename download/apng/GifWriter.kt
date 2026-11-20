package com.CannedF0xy.hideout.download.apng

import android.graphics.Bitmap
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class GifWriter(target: File, private val width: Int, private val height: Int) : Closeable {
    private val out: OutputStream = BufferedOutputStream(FileOutputStream(target))
    private val pixels = IntArray(width * height)
    private val indices = ByteArray(width * height)

    init {
        out.write("GIF89a".toByteArray())
        writeShort(width)
        writeShort(height)
        out.write(0x00)
        out.write(0x00)
        out.write(0x00)
        out.write(byteArrayOf(0x21, 0xFF.toByte(), 0x0B))
        out.write("NETSCAPE2.0".toByteArray())
        out.write(byteArrayOf(0x03, 0x01, 0x00, 0x00, 0x00))
    }

    fun addFrame(bitmap: Bitmap, durationMs: Int) {
        val frame = if (bitmap.width == width && bitmap.height == height) bitmap else Bitmap.createScaledBitmap(bitmap, width, height, true)
        frame.getPixels(pixels, 0, width, 0, 0, width, height)
        if (frame !== bitmap) frame.recycle()

        val palette = buildPalette()
        mapPixels(palette)

        val delay = ((durationMs + 5) / 10).coerceIn(MIN_DELAY_CS, 0xFFFF)
        out.write(byteArrayOf(0x21, 0xF9.toByte(), 0x04))
        out.write((DISPOSE_TO_BACKGROUND shl 2) or 0x01)
        writeShort(delay)
        out.write(TRANSPARENT_INDEX)
        out.write(0x00)

        out.write(0x2C)
        writeShort(0)
        writeShort(0)
        writeShort(width)
        writeShort(height)
        out.write(0x80 or (PALETTE_BITS - 1))
        val table = ByteArray(PALETTE_SIZE * 3)
        for (i in palette.indices) {
            val color = palette[i]
            table[(i + 1) * 3] = (color shr 16).toByte()
            table[(i + 1) * 3 + 1] = (color shr 8).toByte()
            table[(i + 1) * 3 + 2] = color.toByte()
        }
        out.write(table)
        LzwEncoder.encode(indices, PALETTE_BITS, out)
    }

    override fun close() {
        try {
            out.write(0x3B)
            out.flush()
        } finally {
            out.close()
        }
    }

    private fun buildPalette(): IntArray {
        val counts = IntArray(BUCKETS)
        val sumR = LongArray(BUCKETS)
        val sumG = LongArray(BUCKETS)
        val sumB = LongArray(BUCKETS)
        for (p in pixels) {
            if ((p ushr 24) < ALPHA_THRESHOLD) continue
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val bucket = ((r shr 3) shl 10) or ((g shr 3) shl 5) or (b shr 3)
            counts[bucket]++
            sumR[bucket] += r.toLong()
            sumG[bucket] += g.toLong()
            sumB[bucket] += b.toLong()
        }
        val used = (0 until BUCKETS).filter { counts[it] > 0 }.sortedByDescending { counts[it] }.take(PALETTE_SIZE - 1)
        return IntArray(used.size) { i ->
            val bucket = used[i]
            val n = counts[bucket]
            val r = (sumR[bucket] / n).toInt()
            val g = (sumG[bucket] / n).toInt()
            val b = (sumB[bucket] / n).toInt()
            (r shl 16) or (g shl 8) or b
        }
    }

    private fun mapPixels(palette: IntArray) {
        val cache = IntArray(BUCKETS) { -1 }
        for (i in pixels.indices) {
            val p = pixels[i]
            if ((p ushr 24) < ALPHA_THRESHOLD || palette.isEmpty()) {
                indices[i] = TRANSPARENT_INDEX.toByte()
                continue
            }
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val bucket = ((r shr 3) shl 10) or ((g shr 3) shl 5) or (b shr 3)
            var index = cache[bucket]
            if (index < 0) {
                index = nearest(palette, r, g, b) + 1
                cache[bucket] = index
            }
            indices[i] = index.toByte()
        }
    }

    private fun nearest(palette: IntArray, r: Int, g: Int, b: Int): Int {
        var best = 0
        var bestDistance = Int.MAX_VALUE
        for (i in palette.indices) {
            val c = palette[i]
            val dr = ((c shr 16) and 0xFF) - r
            val dg = ((c shr 8) and 0xFF) - g
            val db = (c and 0xFF) - b
            val distance = dr * dr * 2 + dg * dg * 4 + db * db * 3
            if (distance < bestDistance) {
                bestDistance = distance
                best = i
                if (distance == 0) break
            }
        }
        return best
    }

    private fun writeShort(value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
    }

    companion object {
        private const val PALETTE_BITS = 8
        private const val PALETTE_SIZE = 1 shl PALETTE_BITS
        private const val TRANSPARENT_INDEX = 0
        private const val ALPHA_THRESHOLD = 128
        private const val BUCKETS = 1 shl 15
        private const val DISPOSE_TO_BACKGROUND = 2
        private const val MIN_DELAY_CS = 2
    }
}

internal object LzwEncoder {
    private const val MAX_BITS = 12
    private const val MAX_CODES = 1 shl MAX_BITS

    fun encode(data: ByteArray, minCodeSize: Int, out: OutputStream) {
        out.write(minCodeSize)
        val block = SubBlockWriter(out)
        val clearCode = 1 shl minCodeSize
        val endCode = clearCode + 1
        var codeSize = minCodeSize + 1
        var nextCode = endCode + 1
        val table = HashMap<Int, Int>(MAX_CODES * 2)

        block.writeCode(clearCode, codeSize)
        if (data.isEmpty()) {
            block.writeCode(endCode, codeSize)
            block.finish()
            return
        }

        var prefix = data[0].toInt() and 0xFF
        for (i in 1 until data.size) {
            val k = data[i].toInt() and 0xFF
            val key = (prefix shl 8) or k
            val existing = table[key]
            if (existing != null) {
                prefix = existing
                continue
            }
            block.writeCode(prefix, codeSize)
            if (nextCode < MAX_CODES) {
                table[key] = nextCode
                if (nextCode == (1 shl codeSize) && codeSize < MAX_BITS) codeSize++
                nextCode++
            } else {
                block.writeCode(clearCode, codeSize)
                table.clear()
                codeSize = minCodeSize + 1
                nextCode = endCode + 1
            }
            prefix = k
        }
        block.writeCode(prefix, codeSize)
        block.writeCode(endCode, codeSize)
        block.finish()
    }

    private class SubBlockWriter(private val out: OutputStream) {
        private val buffer = ByteArrayOutputStream(255)
        private var bitBuffer = 0
        private var bitCount = 0

        fun writeCode(code: Int, size: Int) {
            bitBuffer = bitBuffer or (code shl bitCount)
            bitCount += size
            while (bitCount >= 8) {
                emit(bitBuffer and 0xFF)
                bitBuffer = bitBuffer ushr 8
                bitCount -= 8
            }
        }

        fun finish() {
            if (bitCount > 0) emit(bitBuffer and 0xFF)
            bitBuffer = 0
            bitCount = 0
            flushBlock()
            out.write(0x00)
        }

        private fun emit(byte: Int) {
            buffer.write(byte)
            if (buffer.size() == 255) flushBlock()
        }

        private fun flushBlock() {
            if (buffer.size() == 0) return
            out.write(buffer.size())
            buffer.writeTo(out)
            buffer.reset()
        }
    }
}
