package com.CannedF0xy.hideout.data

import android.content.Context
import com.CannedF0xy.hideout.network.model.Post
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class LocalCollection(
    val id: Long,
    val name: String,
    val posts: List<Post> = emptyList()
)

object CollectionStore {
    private const val FILE_NAME = "local_collections.json"

    private val mutex = Mutex()
    private val _collections = MutableStateFlow<List<LocalCollection>>(emptyList())
    val collections: StateFlow<List<LocalCollection>> = _collections.asStateFlow()
    private var loaded = false

    suspend fun ensureLoaded(context: Context) = mutex.withLock { loadLocked(context) }

    private suspend fun loadLocked(context: Context) {
        if (loaded) return
        val stored = withContext(Dispatchers.IO) {
            JsonFileStore.read<List<LocalCollection>>(
                context.applicationContext,
                FILE_NAME,
                object : TypeToken<List<LocalCollection>>() {}.type
            )
        }
        _collections.value = stored?.map { it.copy(posts = it.posts ?: emptyList()) } ?: emptyList()
        loaded = true
    }

    suspend fun create(context: Context, name: String): LocalCollection = mutex.withLock {
        loadLocked(context)
        val collection = LocalCollection(id = System.currentTimeMillis(), name = name.trim())
        update(context, _collections.value + collection)
        collection
    }

    suspend fun delete(context: Context, collectionId: Long) = mutex.withLock {
        loadLocked(context)
        update(context, _collections.value.filter { it.id != collectionId })
    }

    suspend fun togglePost(context: Context, collectionId: Long, post: Post) = mutex.withLock {
        loadLocked(context)
        update(context, _collections.value.map { collection ->
            if (collection.id != collectionId) collection
            else if (collection.posts.any { it.id == post.id }) collection.copy(posts = collection.posts.filter { it.id != post.id })
            else collection.copy(posts = listOf(post) + collection.posts)
        })
    }

    suspend fun addPosts(context: Context, collectionId: Long, posts: List<Post>) = mutex.withLock {
        loadLocked(context)
        update(context, _collections.value.map { collection ->
            if (collection.id != collectionId) collection
            else {
                val existingIds = collection.posts.map { it.id }.toSet()
                collection.copy(posts = posts.filter { it.id !in existingIds } + collection.posts)
            }
        })
    }

    suspend fun removePost(context: Context, collectionId: Long, postId: Int) = mutex.withLock {
        loadLocked(context)
        update(context, _collections.value.map { collection ->
            if (collection.id == collectionId) collection.copy(posts = collection.posts.filter { it.id != postId }) else collection
        })
    }

    suspend fun snapshot(context: Context): List<LocalCollection> = mutex.withLock {
        loadLocked(context)
        _collections.value
    }

    suspend fun merge(context: Context, incoming: List<LocalCollection>) = mutex.withLock {
        loadLocked(context)
        val existingIds = _collections.value.map { it.id }.toSet()
        val additions = incoming.filter { it.id !in existingIds }.map { it.copy(posts = it.posts ?: emptyList()) }
        if (additions.isNotEmpty()) update(context, _collections.value + additions)
    }

    private suspend fun update(context: Context, updated: List<LocalCollection>) {
        _collections.value = updated
        withContext(Dispatchers.IO) {
            try {
                JsonFileStore.write(context.applicationContext, FILE_NAME, updated)
            } catch (e: Exception) {
            }
        }
    }
}
