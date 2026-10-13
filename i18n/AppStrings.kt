package com.CannedF0xy.hideout.i18n

import androidx.compose.material3.*
import androidx.compose.runtime.*

interface AppStrings {
    val hideoutSetup: String
    val vpnWarning: String
    val selectConfig: String
    val changeConfig: String
    val enableTunnel: String
    val skipSetup: String
    val fileReadFailed: String
    val parseFailed: String
    val configSaved: String
    val accountLogin: String
    val apiKeyInfo: String
    val usernameHint: String
    val apiKeyHint: String
    val save: String
    val logout: String
    val cancel: String
    val loginFailed: String
    val authFailed: String
    val error403: String
    val errorTooManyTags: String
    val error451: String
    fun networkError(msg: String): String
    val networkErrorOccurred: String
    val blacklistManagement: String
    val blacklistInfo: String
    val tagInputHint: String
    val appSettings: String
    val useDarkTheme: String
    val autoPlayVideo: String
    val downloadLocation: String
    val defaultFolder: String
    val customFolder: String
    val devGithub: String
    val settingsSaved: String
    val close: String
    fun changeLoginInfo(user: String): String
    val accountLoginMenu: String
    val viewFavorites: String
    val weeklyHot: String
    val manageBlacklist: String
    val r18ModeOn: String
    val r18ModeOff: String
    val appSettingsMenu: String
    val disableWireGuard: String
    val enableWireGuard: String
    val searchTags: String
    val latestPosts: String
    fun searchResults(tag: String): String
    val loadFailed: String
    val retry: String
    val noResults: String
    val favRemoved: String
    fun favRemoveFailed(code: Int): String
    val favAdded: String
    val favAddFailedLogin: String
    fun favAddFailed(code: Int): String
    fun fileLabel(ext: String): String
    fun scoreLabel(score: Int): String
    val descLabel: String
    val artistLabel: String
    val copyrightLabel: String
    val characterLabel: String
    val generalLabel: String
    val sourcesLabel: String
    val cannotOpenLink: String
    val savingPost: String
    val downloadStarting: String
    fun downloadFailed(msg: String): String
    val downloadComplete: String
    val loginFirst: String
    val cloudflareCaptcha: String
    val appLanguageLabel: String
    val languageKo: String
    val languageEn: String
    val cdLogo: String
    val cdClose: String
    val cdSearch: String
    val cdAdd: String
    val cdDelete: String
    val cdOpenBrowser: String
    val cdOpenMenu: String
    val cdError: String
    val cdNoResults: String
    val cdBack: String
    val cdFavoriteToggle: String
    val cdDownload: String
    val cdFullscreen: String
    fun idLabel(id: Int): String
    val setupMullvad: String
    val setupWireguard: String
    val mullvadHint: String
    val connectLabel: String
    val loginFailedMullvad: String
    val viewAllRelated: String
    val cacheManagement: String
    val cacheSizeLabel: String
    val clearCache: String
    val cacheCleared: String
    val updateAvailable: String
    fun updateMessage(ver: String): String
    val updateButton: String
    val laterButton: String
    val exportSettings: String
    val importSettings: String
    val exportSuccess: String
    val importSuccess: String
    val importFailed: String
    val commentsTitle: String
    val noComments: String
    fun viewPool(poolId: Int): String
    val unsupportedSwf: String
    val appLockTitle: String
    val appLockDescription: String
    val biometricTitle: String
    val changePin: String
    val secureScreenTitle: String
    val secureScreenDescription: String
    val securitySettingsLabel: String
    val mediaSettingsLabel: String
    val originalQualityTitle: String
    val originalQualityDescription: String
    val lockEnterPin: String
    val lockWrongPin: String
    val lockBiometricTitle: String
    val lockUsePin: String
    val pinSetupTitle: String
    val pinConfirmTitle: String
    val pinMismatch: String
    val cdVoteUp: String
    val cdVoteDown: String
    val voteFailed: String
    val followedTagsMenu: String
    val followedTagsTitle: String
    val followedTagsInfo: String
    val followedTagsEmpty: String
    val followCheckNow: String
    val followCheckNone: String
    val downloadQueueMenu: String
    val downloadQueueTitle: String
    val batchDownloadTitle: String
    val batchNothing: String
    val clearFinished: String
    val cancelAll: String
    val downloadQueueEmpty: String
    val downloadStatusQueued: String
    val downloadStatusDone: String
    val downloadStatusCanceled: String
    val downloadSummaryTitle: String
    val notificationChannelDownloads: String
    val notificationChannelFollow: String
    val poolReaderMode: String
    val poolDownloadAll: String
    val poolEmpty: String
    val videoPageLabel: String
    val searchHistoryTitle: String
    val clearAll: String
    val filterOrderScore: String
    val filterOrderFavcount: String
    val filterOrderRandom: String
    val filterOrderOldest: String
    val filterRatingSafe: String
    val filterRatingQuestionable: String
    val filterRatingExplicit: String
    val blacklistSyncButton: String
    val blacklistSyncFailed: String
    fun downloadNotificationTitle(done: Int, total: Int): String
    fun downloadSummary(done: Int, failed: Int): String
    fun followNotificationTitle(tag: String): String
    fun followNotificationText(count: String): String
    fun lockCooldown(seconds: Int): String
    fun poolPageIndicator(current: Int, total: Int): String
    fun batchQueued(count: Int): String
    fun blacklistSyncResult(added: Int, skipped: Int): String
    fun followAdded(tag: String): String
    fun followCurrentSearch(tag: String): String
    fun followCheckFound(count: Int): String
    fun batchDownloadButton(count: Int): String
    fun downloadQueueListTitle(count: Int): String
    fun downloadStatusRunning(percent: Int): String
    fun downloadStatusFailed(message: String): String
    val autoReconnectTitle: String
    val autoReconnectDescription: String
    val killSwitchTitle: String
    val killSwitchDescription: String
    val vpnSettingsLabel: String
    val vpnReconnecting: String
    val vpnReconnected: String
    val vpnReconnectFailed: String
    val cdSimilarSearch: String
    val similarNotFound: String
    val cdToggleMute: String
    val videoMutedTitle: String
    val collectionsMenu: String
    val collectionsTitle: String
    val collectionsEmpty: String
    val collectionEmpty: String
    val collectionCreate: String
    val collectionNameHint: String
    val collectionPickerTitle: String
    val collectionDeleteTitle: String
    val done: String
    val historyMenu: String
    val historyTitle: String
    val historyEmpty: String
    val historyClearTitle: String
    val historyClearMessage: String
    val incognitoOn: String
    val incognitoOff: String
    val incognitoEnabled: String
    val incognitoDisabled: String
    val tagSearchAction: String
    val tagFollowAction: String
    val tagBlacklistAction: String
    val tagNoWiki: String
    val tagCategorySpecies: String
    val tagCategoryInvalid: String
    val tagCategoryMeta: String
    val tagCategoryLore: String
    val vaultMenu: String
    val vaultTitle: String
    val vaultEmpty: String
    val vaultDeleteTitle: String
    val vaultExport: String
    val vaultExported: String
    val vaultExportFailed: String
    val vaultStorageTitle: String
    val vaultStorageDescription: String
    fun collectionCount(count: Int): String
    fun collectionDeleteMessage(name: String): String
    fun tagPostCount(count: Int): String
    fun tagBlacklisted(tag: String): String
    val cdMoveUp: String
    val cdMoveDown: String
    val dataSaverTitle: String
    val dataSaverDescription: String
    val dataSaverBatchBlocked: String
    val fileNameTemplateLabel: String
    val fileNameTemplateHint: String
    val fileNameTemplateReset: String
    val savedSearchesMenu: String
    val savedSearchesTitle: String
    val savedSearchesInfo: String
    val savedSearchesEmpty: String
    val savedSearchQueryHint: String
    val savedSearchNameHint: String
    val savedSearchAdded: String
    val selectAll: String
    val updateInstallButton: String
    val updateOpenBrowser: String
    val updateNoApk: String
    val updateDownloadFailed: String
    val updateInstallPermission: String
    fun collectionAddedMany(count: Int, name: String): String
    fun fileNameTemplatePreview(example: String): String
    fun selectedCount(count: Int): String
    fun updateDownloading(percent: Int): String
    val imageQualityTitle: String
    val imageQualityDescription: String
    val videoQualityTitle: String
    val videoQualityDescription: String
    fun qualityLabel(key: String): String
}

val LocalStrings = staticCompositionLocalOf<AppStrings> { KoStrings }
