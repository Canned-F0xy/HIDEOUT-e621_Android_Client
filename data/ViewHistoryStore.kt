package com.CannedF0xy.hideout.data

import android.content.Context
import com.CannedF0xy.hideout.network.model.Post
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object ViewHistoryStore {
    private const val FILE_NAME = "view_history.json"
    private const val MAX_ENTRIES = 300
    private const val SAVE_DELAY_MS = 2000L

    private val mutex = Mutex()
    private val writeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    private var loaded = false
    private var pendingSave: Job? = null
    private var dirty = false

    @Volatile
    private var appContext: Context? = null

    suspend fun ensureLoaded(context: Context) = mutex.withLock { loadLocked(context) }

    private suspend fun loadLocked(context: Context) {
        appContext = context.applicationContext
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
            val current = _posts.value
            if (current.firstOrNull()?.id == post.id) return@withLock
            _posts.value = (listOf(post) + current.filter { it.id != post.id }).take(MAX_ENTRIES)
            scheduleSaveLocked()
        }
    }

    suspend fun remove(context: Context, postId: Int) = mutex.withLock {
        loadLocked(context)
        _posts.value = _posts.value.filter { it.id != postId }
        scheduleSaveLocked()
    }

    suspend fun clear(context: Context) = mutex.withLock {
        appContext = context.applicationContext
        pendingSave?.cancel()
        pendingSave = null
        dirty = false
        _posts.value = emptyList()
        loaded = true
        withContext(Dispatchers.IO) { JsonFileStore.delete(context.applicationContext, FILE_NAME) }
    }

    fun flush() {
        writeScope.launch {
            mutex.withLock {
                pendingSave?.cancel()
                pendingSave = null
                writeLocked()
            }
        }
    }

    private fun scheduleSaveLocked() {
        dirty = true
        pendingSave?.cancel()
        pendingSave = writeScope.launch {
            delay(SAVE_DELAY_MS)
            mutex.withLock {
                pendingSave = null
                writeLocked()
            }
        }
    }

    private fun writeLocked() {
        if (!dirty) return
        val context = appContext ?: return
        try {
            JsonFileStore.write(context, FILE_NAME, _posts.value)
            dirty = false
        } catch (e: Exception) {
        }
    }
}
