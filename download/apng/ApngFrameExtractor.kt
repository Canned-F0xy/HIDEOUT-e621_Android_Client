package com.CannedF0xy.hideout.download.apng

import android.graphics.Bitmap
import com.github.penfeizhou.animation.apng.decode.APNGDecoder
import com.github.penfeizhou.animation.apng.io.APNGReader
import com.github.penfeizhou.animation.apng.io.APNGWriter
import com.github.penfeizhou.animation.decode.Frame
import com.github.penfeizhou.animation.loader.FileLoader
import java.io.File

private class CapturingApngDecoder(
    path: String,
    private val onFrame: (Bitmap, Int) -> Unit
) : APNGDecoder(FileLoader(path), null) {

    var failure: Throwable? = null
        private set

    override fun renderFrame(frame: Frame<APNGReader, APNGWriter>?) {
        super.renderFrame(frame)
        if (frame == null || failure != null) return
        val rect = fullRect ?: return
        if (rect.width() <= 0 || rect.height() <= 0) return
        try {
            val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
            frameBuffer.rewind()
            bitmap.copyPixelsFromBuffer(frameBuffer)
            try {
                onFrame(bitmap, frame.frameDuration)
            } finally {
                bitmap.recycle()
            }
        } catch (t: Throwable) {
            failure = t
        }
    }
}

object ApngFrameExtractor {
    fun extract(file: File, onFrame: (Bitmap, Int) -> Unit) {
        val decoder = CapturingApngDecoder(file.absolutePath, onFrame)
        decoder.getFrameBitmap(-1)?.recycle()
        decoder.failure?.let { throw it }
    }
}
