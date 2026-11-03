package com.CannedF0xy.hideout.util

import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import com.github.penfeizhou.animation.apng.APNGDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.ByteString.Companion.decodeHex

class ApngDecoder(private val source: ImageSource) : Decoder {

    override suspend fun decode(): DecodeResult = withContext(Dispatchers.IO) {
        val path = source.file().toFile().absolutePath
        DecodeResult(drawable = APNGDrawable.fromFile(path), isSampled = false)
    }

    class Factory : Decoder.Factory {
        override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder? {
            return if (isAnimatedPng(result.source.source())) ApngDecoder(result.source) else null
        }
    }

    companion object {
        private val PNG_SIGNATURE = "89504e470d0a1a0a".decodeHex()
        private const val MAX_CHUNKS_TO_SCAN = 64

        fun isAnimatedPng(source: BufferedSource): Boolean {
            return try {
                val peek = source.peek()
                if (!peek.rangeEquals(0, PNG_SIGNATURE)) return false
                peek.skip(PNG_SIGNATURE.size.toLong())
                repeat(MAX_CHUNKS_TO_SCAN) {
                    if (peek.exhausted()) return false
                    val length = peek.readInt().toLong() and 0xFFFFFFFFL
                    when (peek.readUtf8(4)) {
                        "acTL" -> return true
                        "IDAT" -> return false
                    }
                    peek.skip(length + 4)
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
}
