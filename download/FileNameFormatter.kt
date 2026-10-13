package com.CannedF0xy.hideout.download

import com.CannedF0xy.hideout.network.model.Post

object FileNameFormatter {
    const val DEFAULT_TEMPLATE = "Hideout_{id}"
    val TOKENS = listOf("{id}", "{artist}", "{character}", "{copyright}", "{species}", "{rating}", "{date}", "{score}", "{md5}")
    val PRESETS = listOf(DEFAULT_TEMPLATE, "{artist}_{id}", "{date}_{id}", "{copyright}_{character}_{id}")

    private const val MAX_LENGTH = 120
    private val IGNORED_ARTISTS = setOf(
        "conditional_dnp", "sound_warning", "unknown_artist", "anonymous_artist",
        "third-party_edit", "epilepsy_warning", "avoid_posting", "unknown_artist_signature"
    )
    private val INVALID_CHARS = Regex("[\\\\/:*?\"<>|\\s]+")

    fun format(template: String, post: Post, ext: String): String {
        val base = buildBase(template, post)
        return if (ext.isBlank()) base else "$base.$ext"
    }

    fun formatForPool(template: String, post: Post, ext: String, poolId: Int, index: Int): String {
        val base = if (template.isBlank() || template == DEFAULT_TEMPLATE) {
            "Hideout_pool${poolId}_${index}_${post.id}"
        } else {
            "pool${poolId}_${index}_" + buildBase(template, post)
        }
        return if (ext.isBlank()) base else "$base.$ext"
    }

    private fun buildBase(template: String, post: Post): String {
        val source = template.ifBlank { DEFAULT_TEMPLATE }
        val artist = post.tags?.artist?.firstOrNull { it !in IGNORED_ARTISTS } ?: "unknown"
        val character = post.tags?.character?.firstOrNull() ?: "none"
        val copyright = post.tags?.copyright?.firstOrNull() ?: "none"
        val species = post.tags?.species?.firstOrNull() ?: "none"
        val rating = when (post.rating?.lowercase()?.take(1)) {
            "s" -> "safe"
            "q" -> "questionable"
            "e" -> "explicit"
            else -> "unrated"
        }
        val date = post.created_at?.take(10) ?: "nodate"
        val result = source
            .replace("{id}", post.id.toString())
            .replace("{artist}", artist)
            .replace("{character}", character)
            .replace("{copyright}", copyright)
            .replace("{species}", species)
            .replace("{rating}", rating)
            .replace("{date}", date)
            .replace("{score}", (post.score?.total ?: 0).toString())
            .replace("{md5}", post.file?.md5 ?: post.id.toString())
        val cleaned = INVALID_CHARS.replace(result, "_").trim('_', '.').take(MAX_LENGTH)
        return cleaned.ifBlank { "Hideout_${post.id}" }
    }
}
