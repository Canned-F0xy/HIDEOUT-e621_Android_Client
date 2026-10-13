package com.CannedF0xy.hideout.data

import android.content.Context
import com.CannedF0xy.hideout.network.model.Post
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object ViewHistoryStore {
    private const val FILE_NAME = "view_history.json"
    private const val MAX_ENTRIES = 300

    private val mutex = Mutex()
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    private var loaded = false

    suspend fun ensureLoaded(context: Context) = mutex.withLock { loadLocked(context) }

    private suspend fun loadLocked(context: Context) {
        if (loaded) return
        val stored = withContext(Dispatchers.IO) {
            JsonFileStore.read<List<Post>>(context.applicationContext, FILE_NAME, object : TypeToken<List<Post>>() {}.type)
        }
        _posts.value = stored ?: emptyList()
        loaded = true
    }

    suspend fun record(context: Context, post: Post) {
        if (AppPreferences(context.applicationContext).isIncognito.first()) return
        mutex.withLock {
            loadLocked(context)
            val updated = (listOf(post) + _posts.value.filter { it.id != post.id }).take(MAX_ENTRIES)
            _posts.value = updated
            persist(context, updated)
        }
    }

    suspend fun remove(context: Context, postId: Int) = mutex.withLock {
        loadLocked(context)
        val updated = _posts.value.filter { it.id != postId }
        _posts.value = updated
        persist(context, updated)
    }

    suspend fun clear(context: Context) = mutex.withLock {
        _posts.value = emptyList()
        loaded = true
        withContext(Dispatchers.IO) { JsonFileStore.delete(context.applicationContext, FILE_NAME) }
    }

    private suspend fun persist(context: Context, posts: List<Post>) {
        withContext(Dispatchers.IO) {
            try {
                JsonFileStore.write(context.applicationContext, FILE_NAME, posts)
            } catch (e: Exception) {
            }
        }
    }
}
