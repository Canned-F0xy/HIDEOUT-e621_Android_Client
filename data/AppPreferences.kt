package com.CannedF0xy.hideout.data

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.CannedF0xy.hideout.download.FileNameFormatter
import com.CannedF0xy.hideout.util.MediaQuality
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "hideout_datastore")

class AppPreferences(private val context: Context) {
    companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_AUTO_PLAY = booleanPreferencesKey("auto_play_video")
        val KEY_NSFW_ENABLED = booleanPreferencesKey("is_nsfw_enabled")
        val KEY_DOWNLOAD_TREE_URI = stringPreferencesKey("download_tree_uri")
        val KEY_BLACKLIST = stringSetPreferencesKey("blacklisted_tags")
        val KEY_SEARCH_STACK = stringPreferencesKey("search_stack")
        val KEY_VPN_BYPASSED = booleanPreferencesKey("is_vpn_bypassed")

        val KEY_ENC_USERNAME = stringPreferencesKey("enc_username")
        val KEY_ENC_API_KEY = stringPreferencesKey("enc_api_key")
        val KEY_ENC_CF_CLEARANCE = stringPreferencesKey("enc_cf_clearance")
        val KEY_ENC_VPN_CONFIG = stringPreferencesKey("enc_vpn_config")
        val KEY_ENC_MULLVAD_PRIV = stringPreferencesKey("enc_mullvad_priv")
        val KEY_ENC_MULLVAD_PUB = stringPreferencesKey("enc_mullvad_pub")

        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_SECURE_SCREEN = booleanPreferencesKey("secure_screen")
        val KEY_ENC_PIN = stringPreferencesKey("enc_app_pin")
        val KEY_ORIGINAL_QUALITY = booleanPreferencesKey("original_quality")
        val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")
        val KEY_FOLLOWED_TAGS = stringPreferencesKey("followed_tags")
        val KEY_KILL_SWITCH = booleanPreferencesKey("vpn_kill_switch")
        val KEY_AUTO_RECONNECT = booleanPreferencesKey("vpn_auto_reconnect")
        val KEY_INCOGNITO = booleanPreferencesKey("incognito_mode")
        val KEY_DATA_SAVER = booleanPreferencesKey("data_saver_mode")
        val KEY_IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val KEY_VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val KEY_FILENAME_TEMPLATE = stringPreferencesKey("filename_template")
        val KEY_SAVED_SEARCHES = stringPreferencesKey("saved_searches")

        private const val MAX_SEARCH_HISTORY = 20
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_THEME] ?: true }
    val appLanguage: Flow<String> = context.dataStore.data.map { it[KEY_APP_LANGUAGE] ?: "ko" }
    val isAutoPlay: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_PLAY] ?: true }
    val isNsfwEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_NSFW_ENABLED] ?: false }
    val downloadTreeUri: Flow<String> = context.dataStore.data.map { it[KEY_DOWNLOAD_TREE_URI] ?: "" }
    val blacklistedTags: Flow<Set<String>> = context.dataStore.data.map { it[KEY_BLACKLIST] ?: emptySet() }
    val searchStack: Flow<String> = context.dataStore.data.map { it[KEY_SEARCH_STACK] ?: "" }
    val isVpnBypassed: Flow<Boolean> = context.dataStore.data.map { it[KEY_VPN_BYPASSED] ?: false }

    val username: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_USERNAME] ?: "") }
    val apiKey: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_API_KEY] ?: "") }
    val cfClearance: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_CF_CLEARANCE] ?: "") }
    val vpnConfigText: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_VPN_CONFIG] ?: "") }
    val mullvadPrivKey: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_MULLVAD_PRIV] ?: "") }
    val mullvadPubKey: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_MULLVAD_PUB] ?: "") }

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_APP_LOCK_ENABLED] ?: false }
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_BIOMETRIC_ENABLED] ?: false }
    val isSecureScreen: Flow<Boolean> = context.dataStore.data.map { it[KEY_SECURE_SCREEN] ?: false }
    val pinRecord: Flow<String> = context.dataStore.data.map { CryptoHelper.decrypt(it[KEY_ENC_PIN] ?: "") }
    val isOriginalQuality: Flow<Boolean> = context.dataStore.data.map { it[KEY_ORIGINAL_QUALITY] ?: true }
    val searchHistory: Flow<List<String>> = context.dataStore.data.map { decodeStringList(it[KEY_SEARCH_HISTORY]) }
    val isKillSwitchEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_KILL_SWITCH] ?: false }
    val isAutoReconnectEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_RECONNECT] ?: true }
    val isIncognito: Flow<Boolean> = context.dataStore.data.map { it[KEY_INCOGNITO] ?: false }
    val imageQuality: Flow<MediaQuality> = context.dataStore.data.map {
        val stored = it[KEY_IMAGE_QUALITY]
        if (stored != null) MediaQuality.fromKey(stored)
        else if (it[KEY_ORIGINAL_QUALITY] == false) MediaQuality.HIGH
        else MediaQuality.DEFAULT
    }
    val videoQuality: Flow<MediaQuality> = context.dataStore.data.map { MediaQuality.fromKey(it[KEY_VIDEO_QUALITY]) }
    val isDataSaverEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_DATA_SAVER] ?: false }
    val fileNameTemplate: Flow<String> = context.dataStore.data.map { it[KEY_FILENAME_TEMPLATE] ?: FileNameFormatter.DEFAULT_TEMPLATE }
    val savedSearches: Flow<List<SavedSearch>> = context.dataStore.data.map { decodeSavedSearches(it[KEY_SAVED_SEARCHES]) }
    val followedTags: Flow<Map<String, Int>> = context.dataStore.data.map { decodeFollowMap(it[KEY_FOLLOWED_TAGS]) }

    suspend fun readStartupSnapshot(): StartupSnapshot {
        val data = context.dataStore.data.first()
        val lockEnabled = data[KEY_APP_LOCK_ENABLED] ?: false
        return StartupSnapshot(
            isAppLockEnabled = lockEnabled,
            pinRecord = if (lockEnabled) CryptoHelper.decrypt(data[KEY_ENC_PIN] ?: "") else "",
            isSecureScreen = data[KEY_SECURE_SCREEN] ?: false,
            isIncognito = data[KEY_INCOGNITO] ?: false,
            appLanguage = data[KEY_APP_LANGUAGE] ?: "ko",
            isDarkTheme = data[KEY_DARK_THEME] ?: true
        )
    }

    suspend fun setDarkTheme(value: Boolean) = context.dataStore.edit { it[KEY_DARK_THEME] = value }
    suspend fun setAppLanguage(value: String) = context.dataStore.edit { it[KEY_APP_LANGUAGE] = value }
    suspend fun setAutoPlay(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_PLAY] = value }
    suspend fun setNsfwEnabled(value: Boolean) = context.dataStore.edit { it[KEY_NSFW_ENABLED] = value }
    suspend fun setDownloadTreeUri(value: String) = context.dataStore.edit { it[KEY_DOWNLOAD_TREE_URI] = value }
    suspend fun setBlacklist(value: Set<String>) = context.dataStore.edit { it[KEY_BLACKLIST] = value }
    suspend fun setSearchStack(value: String) = context.dataStore.edit {
        if (it[KEY_INCOGNITO] == true) it.remove(KEY_SEARCH_STACK) else it[KEY_SEARCH_STACK] = value
    }
    suspend fun setVpnBypassed(value: Boolean) = context.dataStore.edit { it[KEY_VPN_BYPASSED] = value }

    suspend fun setCredentials(user: String, key: String) = context.dataStore.edit {
        it[KEY_ENC_USERNAME] = CryptoHelper.encrypt(user)
        it[KEY_ENC_API_KEY] = CryptoHelper.encrypt(key)
    }

    suspend fun setCfClearance(cookie: String) = context.dataStore.edit {
        it[KEY_ENC_CF_CLEARANCE] = CryptoHelper.encrypt(cookie)
    }

    suspend fun setVpnConfig(configText: String) = context.dataStore.edit {
        it[KEY_ENC_VPN_CONFIG] = CryptoHelper.encrypt(configText)
    }

    suspend fun setMullvadKeys(priv: String, pub: String) = context.dataStore.edit {
        it[KEY_ENC_MULLVAD_PRIV] = CryptoHelper.encrypt(priv)
        it[KEY_ENC_MULLVAD_PUB] = CryptoHelper.encrypt(pub)
    }

    suspend fun setAppLock(enabled: Boolean, pinRecord: String?) = context.dataStore.edit {
        it[KEY_APP_LOCK_ENABLED] = enabled
        if (pinRecord != null) it[KEY_ENC_PIN] = CryptoHelper.encrypt(pinRecord)
        if (!enabled) {
            it.remove(KEY_ENC_PIN)
            it[KEY_BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setImageQuality(value: MediaQuality) = context.dataStore.edit { it[KEY_IMAGE_QUALITY] = value.key }
    suspend fun setVideoQuality(value: MediaQuality) = context.dataStore.edit { it[KEY_VIDEO_QUALITY] = value.key }
    suspend fun setDataSaver(value: Boolean) = context.dataStore.edit { it[KEY_DATA_SAVER] = value }
    suspend fun setFileNameTemplate(value: String) = context.dataStore.edit { it[KEY_FILENAME_TEMPLATE] = value }

    suspend fun addSavedSearch(name: String, query: String) = context.dataStore.edit {
        val current = decodeSavedSearches(it[KEY_SAVED_SEARCHES])
        val entry = SavedSearch(name.trim().ifEmpty { query.trim() }, query.trim())
        it[KEY_SAVED_SEARCHES] = Gson().toJson(current.filter { s -> s.query != entry.query } + entry)
    }

    suspend fun removeSavedSearch(query: String) = context.dataStore.edit {
        val updated = decodeSavedSearches(it[KEY_SAVED_SEARCHES]).filter { s -> s.query != query }
        it[KEY_SAVED_SEARCHES] = Gson().toJson(updated)
    }

    suspend fun moveSavedSearch(fromIndex: Int, toIndex: Int) = context.dataStore.edit {
        val list = decodeSavedSearches(it[KEY_SAVED_SEARCHES]).toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            it[KEY_SAVED_SEARCHES] = Gson().toJson(list)
        }
    }

    private fun decodeSavedSearches(raw: String?): List<SavedSearch> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            Gson().fromJson(raw, Array<SavedSearch>::class.java)?.filter { it.query != null && it.query.isNotBlank() }?.map {
                SavedSearch(it.name ?: it.query, it.query)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setKillSwitch(value: Boolean) = context.dataStore.edit { it[KEY_KILL_SWITCH] = value }
    suspend fun setAutoReconnect(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_RECONNECT] = value }
    suspend fun setIncognito(value: Boolean) = context.dataStore.edit {
        it[KEY_INCOGNITO] = value
        if (value) it.remove(KEY_SEARCH_STACK)
    }

    suspend fun setBiometricEnabled(value: Boolean) = context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = value }
    suspend fun setSecureScreen(value: Boolean) = context.dataStore.edit { it[KEY_SECURE_SCREEN] = value }
    suspend fun setOriginalQuality(value: Boolean) = context.dataStore.edit { it[KEY_ORIGINAL_QUALITY] = value }

    suspend fun addSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit {
            if (it[KEY_INCOGNITO] == true) return@edit
            val current = decodeStringList(it[KEY_SEARCH_HISTORY])
            val updated = (listOf(trimmed) + current.filter { q -> q != trimmed }).take(MAX_SEARCH_HISTORY)
            it[KEY_SEARCH_HISTORY] = Gson().toJson(updated)
        }
    }

    suspend fun removeSearchHistory(query: String) = context.dataStore.edit {
        val updated = decodeStringList(it[KEY_SEARCH_HISTORY]).filter { q -> q != query }
        it[KEY_SEARCH_HISTORY] = Gson().toJson(updated)
    }

    suspend fun clearSearchHistory() = context.dataStore.edit { it.remove(KEY_SEARCH_HISTORY) }

    suspend fun setFollowedTag(tag: String, lastSeenId: Int) = context.dataStore.edit {
        val updated = decodeFollowMap(it[KEY_FOLLOWED_TAGS]).toMutableMap()
        updated[tag] = lastSeenId
        it[KEY_FOLLOWED_TAGS] = Gson().toJson(updated)
    }

    suspend fun updateFollowedTagIds(updates: Map<String, Int>) = context.dataStore.edit {
        val current = decodeFollowMap(it[KEY_FOLLOWED_TAGS]).toMutableMap()
        updates.forEach { (tag, id) -> if (current.containsKey(tag)) current[tag] = id }
        it[KEY_FOLLOWED_TAGS] = Gson().toJson(current)
    }

    suspend fun removeFollowedTag(tag: String) = context.dataStore.edit {
        val updated = decodeFollowMap(it[KEY_FOLLOWED_TAGS]).toMutableMap()
        updated.remove(tag)
        it[KEY_FOLLOWED_TAGS] = Gson().toJson(updated)
    }

    private fun decodeStringList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            Gson().fromJson(raw, Array<String>::class.java)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun decodeFollowMap(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JsonParser().parse(raw).asJsonObject
            obj.entrySet().associate { entry -> entry.key to entry.value.asInt }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun exportSettingsBase64(): String {
        val data = context.dataStore.data.first()
        val exportObj = ExportSettings(
            darkTheme = data[KEY_DARK_THEME] ?: true,
            lang = data[KEY_APP_LANGUAGE] ?: "ko",
            downloadTreeUri = data[KEY_DOWNLOAD_TREE_URI] ?: "",
            blacklist = (data[KEY_BLACKLIST] ?: emptySet()).toList(),
            autoPlay = data[KEY_AUTO_PLAY] ?: true,
            followedTags = decodeFollowMap(data[KEY_FOLLOWED_TAGS]),
            searchHistory = decodeStringList(data[KEY_SEARCH_HISTORY]),
            secureScreen = data[KEY_SECURE_SCREEN] ?: false,
            killSwitch = data[KEY_KILL_SWITCH] ?: false,
            autoReconnect = data[KEY_AUTO_RECONNECT] ?: true,
            incognito = data[KEY_INCOGNITO] ?: false,
            originalQuality = data[KEY_ORIGINAL_QUALITY] ?: true,
            dataSaver = data[KEY_DATA_SAVER] ?: false,
            imageQuality = data[KEY_IMAGE_QUALITY] ?: MediaQuality.DEFAULT.key,
            videoQuality = data[KEY_VIDEO_QUALITY] ?: MediaQuality.DEFAULT.key,
            fileNameTemplate = data[KEY_FILENAME_TEMPLATE] ?: FileNameFormatter.DEFAULT_TEMPLATE,
            savedSearches = decodeSavedSearches(data[KEY_SAVED_SEARCHES]),
            collections = CollectionStore.snapshot(context)
        )
        val jsonString = Gson().toJson(exportObj)
        val base64Str = Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return Gson().toJson(mapOf("backup_data" to base64Str))
    }

    suspend fun importSettingsBase64(fileContent: String): Boolean {
        return try {
            val wrapper = com.google.gson.JsonParser().parse(fileContent.trim()).asJsonObject
            val base64Str = wrapper.get("backup_data").asString
            val jsonString = String(Base64.decode(base64Str.trim(), Base64.NO_WRAP), Charsets.UTF_8)
            val importObj = Gson().fromJson(jsonString, ExportSettings::class.java)
            context.dataStore.edit {
                it[KEY_DARK_THEME] = importObj.darkTheme
                it[KEY_APP_LANGUAGE] = importObj.lang
                it[KEY_DOWNLOAD_TREE_URI] = importObj.downloadTreeUri
                it[KEY_BLACKLIST] = importObj.blacklist.toSet()
                importObj.autoPlay?.let { v -> it[KEY_AUTO_PLAY] = v }
                importObj.followedTags?.let { v ->
                    val merged = decodeFollowMap(it[KEY_FOLLOWED_TAGS]) + v.filterKeys { tag -> !decodeFollowMap(it[KEY_FOLLOWED_TAGS]).containsKey(tag) }
                    it[KEY_FOLLOWED_TAGS] = Gson().toJson(merged)
                }
                importObj.searchHistory?.let { v -> it[KEY_SEARCH_HISTORY] = Gson().toJson(v.take(MAX_SEARCH_HISTORY)) }
                importObj.secureScreen?.let { v -> it[KEY_SECURE_SCREEN] = v }
                importObj.killSwitch?.let { v -> it[KEY_KILL_SWITCH] = v }
                importObj.autoReconnect?.let { v -> it[KEY_AUTO_RECONNECT] = v }
                importObj.incognito?.let { v -> it[KEY_INCOGNITO] = v }
                importObj.originalQuality?.let { v -> it[KEY_ORIGINAL_QUALITY] = v }
                importObj.dataSaver?.let { v -> it[KEY_DATA_SAVER] = v }
                importObj.imageQuality?.let { v -> it[KEY_IMAGE_QUALITY] = MediaQuality.fromKey(v).key }
                importObj.videoQuality?.let { v -> it[KEY_VIDEO_QUALITY] = MediaQuality.fromKey(v).key }
                importObj.fileNameTemplate?.let { v -> it[KEY_FILENAME_TEMPLATE] = v }
                importObj.savedSearches?.let { v ->
                    val existing = decodeSavedSearches(it[KEY_SAVED_SEARCHES])
                    val merged = existing + v.filter { s -> s.query != null && existing.none { e -> e.query == s.query } }
                    it[KEY_SAVED_SEARCHES] = Gson().toJson(merged)
                }
            }
            importObj.collections?.let { CollectionStore.merge(context, it) }
            true
        } catch (e: Exception) {
            false
        }
    }
}

data class ExportSettings(
    val darkTheme: Boolean,
    val lang: String,
    val downloadTreeUri: String,
    val blacklist: List<String>,
    val autoPlay: Boolean? = null,
    val followedTags: Map<String, Int>? = null,
    val searchHistory: List<String>? = null,
    val secureScreen: Boolean? = null,
    val killSwitch: Boolean? = null,
    val autoReconnect: Boolean? = null,
    val incognito: Boolean? = null,
    val originalQuality: Boolean? = null,
    val dataSaver: Boolean? = null,
    val imageQuality: String? = null,
    val videoQuality: String? = null,
    val fileNameTemplate: String? = null,
    val savedSearches: List<SavedSearch>? = null,
    val collections: List<LocalCollection>? = null
)

data class SavedSearch(
    val name: String,
    val query: String
)

data class StartupSnapshot(
    val isAppLockEnabled: Boolean,
    val pinRecord: String,
    val isSecureScreen: Boolean,
    val isIncognito: Boolean,
    val appLanguage: String,
    val isDarkTheme: Boolean
)
