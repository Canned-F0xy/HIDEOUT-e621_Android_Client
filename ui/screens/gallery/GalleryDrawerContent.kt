package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.R
import com.CannedF0xy.hideout.i18n.LocalStrings

@Composable
fun GalleryDrawerContent(
    loginUsername: String,
    isNsfwEnabled: Boolean,
    onLoginClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHotPostsClick: () -> Unit,
    onBlacklistClick: () -> Unit,
    onNsfwClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFollowClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    isIncognito: Boolean = false,
    onIncognitoClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onCollectionsClick: () -> Unit = {},
    onVaultClick: () -> Unit = {},
    onSavedSearchesClick: () -> Unit = {}
) {
    val strings = LocalStrings.current
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Image(painter = painterResource(id = R.drawable.ic_launcher2), contentDescription = strings.cdLogo, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
                Spacer(modifier = Modifier.height(12.dp))
                Text("HIDEOUT", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.primary)
                Text("Ver. 2026-10-13", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            NavigationDrawerItem(
                label = { Text(if (loginUsername.isNotBlank()) strings.changeLoginInfo(loginUsername) else strings.accountLoginMenu) },
                selected = false,
                onClick = onLoginClick,
                icon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.viewFavorites) },
                selected = false,
                onClick = onFavoritesClick,
                icon = { Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.weeklyHot) },
                selected = false,
                onClick = onHotPostsClick,
                icon = { Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.manageBlacklist) },
                selected = false,
                onClick = onBlacklistClick,
                icon = { Icon(Icons.Default.Block, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(if (isNsfwEnabled) strings.r18ModeOn else strings.r18ModeOff, color = if (isNsfwEnabled) Color.Red else MaterialTheme.colorScheme.onSurface) },
                selected = false,
                onClick = onNsfwClick,
                icon = { Icon(Icons.Default.Warning, null, tint = if (isNsfwEnabled) Color.Red else MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.followedTagsMenu) },
                selected = false,
                onClick = onFollowClick,
                icon = { Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.downloadQueueMenu) },
                selected = false,
                onClick = onDownloadsClick,
                icon = { Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.savedSearchesMenu) },
                selected = false,
                onClick = onSavedSearchesClick,
                icon = { Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.collectionsMenu) },
                selected = false,
                onClick = onCollectionsClick,
                icon = { Icon(Icons.Default.CollectionsBookmark, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.historyMenu) },
                selected = false,
                onClick = onHistoryClick,
                icon = { Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(strings.vaultMenu) },
                selected = false,
                onClick = onVaultClick,
                icon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
            NavigationDrawerItem(
                label = { Text(if (isIncognito) strings.incognitoOn else strings.incognitoOff, color = if (isIncognito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                selected = false,
                onClick = onIncognitoClick,
                icon = { Icon(Icons.Default.VisibilityOff, null, tint = if (isIncognito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(50)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        NavigationDrawerItem(
            label = { Text(strings.appSettingsMenu) },
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            shape = RoundedCornerShape(50)
        )
    }
}
