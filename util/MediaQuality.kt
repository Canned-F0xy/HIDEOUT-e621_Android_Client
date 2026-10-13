package com.CannedF0xy.hideout.util

import com.CannedF0xy.hideout.network.model.Post

enum class MediaQuality(val key: String) {
    ORIGINAL("original"),
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    companion object {
        val DEFAULT = ORIGINAL
        fun fromKey(key: String?): MediaQuality = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

data class ImageSource(val url: String, val maxDimensionPx: Int?)

object MediaUrlResolver {
    private const val MEDIUM_MAX_PX = 720

    fun capForDataSaver(quality: MediaQuality, cap: MediaQuality, isSaving: Boolean): MediaQuality {
        return if (isSaving && quality.ordinal < cap.ordinal) cap else quality
    }

    fun imageSource(post: Post, quality: MediaQuality): ImageSource? {
        val original = post.file?.url
        val sample = post.sample?.url
        val preview = post.preview?.url
        return when (quality) {
            MediaQuality.ORIGINAL -> (original ?: sample ?: preview)?.let { ImageSource(it, null) }
            MediaQuality.HIGH -> (sample ?: original ?: preview)?.let { ImageSource(it, null) }
            MediaQuality.MEDIUM -> (sample ?: original ?: preview)?.let { ImageSource(it, MEDIUM_MAX_PX) }
            MediaQuality.LOW -> (preview ?: sample ?: original)?.let { ImageSource(it, null) }
        }
    }

    fun videoUrl(post: Post, quality: MediaQuality): String? {
        val original = post.file?.url
        if (quality == MediaQuality.ORIGINAL) return original
        val variants = (post.sample?.videoVariants() ?: emptyList())
            .filter { it.url != original }
            .sortedByDescending { it.height }
            .map { it.height to it.url }
        if (variants.isEmpty()) return original
        val picked = when (quality) {
            MediaQuality.HIGH -> variants.first()
            MediaQuality.MEDIUM -> variants.getOrNull(1) ?: variants.last()
            MediaQuality.LOW -> variants.last()
            MediaQuality.ORIGINAL -> null
        }
        return picked?.second ?: original
    }
}
