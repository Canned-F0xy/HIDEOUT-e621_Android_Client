package com.CannedF0xy.hideout.network

import com.CannedF0xy.hideout.network.model.Post

data class SearchQuery(
    val tags: String,
    val clientSideBlacklist: Set<String>
) {
    fun isAllowed(post: Post): Boolean {
        if (clientSideBlacklist.isEmpty()) return true
        val postTags = post.allTags()
        val rating = post.rating?.lowercase()?.take(1)
        for (tag in clientSideBlacklist) {
            if (tag.startsWith("rating:")) {
                val wanted = tag.removePrefix("rating:").lowercase().take(1)
                if (rating != null && rating == wanted) return false
            } else if (postTags.contains(tag)) {
                return false
            }
        }
        return true
    }

    fun filter(posts: List<Post>): List<Post> = posts.filter { isAllowed(it) }
}

object SearchQueryBuilder {
    const val HOT_QUERY = "#HOT#"
    private const val MAX_SERVER_TAGS = 38

    fun build(query: String, isNsfwEnabled: Boolean, blacklistedTags: List<String>): SearchQuery {
        var finalTag = if (query == HOT_QUERY) "date:>=7_days_ago order:score" else query.trim()
        if (!isNsfwEnabled && !finalTag.contains("rating:")) {
            finalTag = if (finalTag.isEmpty()) "rating:safe" else "$finalTag rating:safe"
        }

        if (blacklistedTags.isEmpty()) return SearchQuery(finalTag, emptySet())

        val usedSlots = finalTag.split(" ").count { it.isNotBlank() }
        val budget = (MAX_SERVER_TAGS - usedSlots).coerceAtLeast(0)
        val ordered = blacklistedTags.sortedByDescending { it.contains(":") }
        val serverSide = ordered.take(budget)
        val clientSide = ordered.drop(budget).filter { !it.contains(":") || it.startsWith("rating:") }.toSet()

        if (serverSide.isNotEmpty()) {
            val negatedTags = serverSide.joinToString(" ") { "-$it" }
            finalTag = if (finalTag.isEmpty()) negatedTags else "$finalTag $negatedTags"
        }
        return SearchQuery(finalTag, clientSide)
    }
}
