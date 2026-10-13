package com.CannedF0xy.hideout.network

import com.CannedF0xy.hideout.network.model.Pool
import com.CannedF0xy.hideout.network.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PoolContent(
    val pool: Pool,
    val posts: List<Post>
)

object PoolRepository {
    private const val CHUNK_SIZE = 100

    fun parsePoolId(query: String): Int? {
        val match = Regex("^pool:(\\d+)$").find(query.trim()) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    suspend fun load(poolId: Int, isNsfwEnabled: Boolean, blacklistedTags: List<String>): PoolContent = withContext(Dispatchers.IO) {
        val api = NetworkModule.api!!
        val pool = api.getPool(poolId)
        val ids = pool.post_ids ?: emptyList()
        val collected = HashMap<Int, Post>()
        ids.chunked(CHUNK_SIZE).forEach { chunk ->
            val query = SearchQueryBuilder.build("id:" + chunk.joinToString(","), isNsfwEnabled, blacklistedTags)
            val response = api.getPosts(tags = query.tags, limit = chunk.size, page = 1)
            query.filter(response.posts ?: emptyList()).forEach { collected[it.id] = it }
        }
        PoolContent(pool, ids.mapNotNull { collected[it] })
    }
}
