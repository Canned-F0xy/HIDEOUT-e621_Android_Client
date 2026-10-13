package com.CannedF0xy.hideout.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.follow.FollowChecker
import com.CannedF0xy.hideout.i18n.AppStrings
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.TagInfo
import com.CannedF0xy.hideout.network.TagInfoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun TagInfoDialog(
    tag: String,
    appPrefs: AppPreferences,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var info by remember(tag) { mutableStateOf<TagInfo?>(null) }
    var failed by remember(tag) { mutableStateOf(false) }

    LaunchedEffect(tag) {
        try {
            info = TagInfoRepository.load(tag)
        } catch (e: Exception) {
            failed = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(tag.replace('_', ' '), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                val loaded = info
                when {
                    failed -> Text(strings.loadFailed, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    loaded == null -> Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
                    }
                    else -> {
                        Text(
                            listOfNotNull(categoryLabel(loaded.category, strings), loaded.postCount?.let { strings.tagPostCount(it) }).joinToString(" · "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                            Text(loaded.wikiText ?: strings.tagNoWiki, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { onSearch(tag) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(strings.tagSearchAction, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val nsfw = appPrefs.isNsfwEnabled.first()
                                val blacklist = appPrefs.blacklistedTags.first().toList()
                                val newestId = FollowChecker.fetchNewestId(tag, nsfw, blacklist) ?: 0
                                appPrefs.setFollowedTag(tag, newestId)
                                Toast.makeText(context, strings.followAdded(tag), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(strings.tagFollowAction, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary) }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val current = appPrefs.blacklistedTags.first()
                                appPrefs.setBlacklist(current + tag)
                                Toast.makeText(context, strings.tagBlacklisted(tag), Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(strings.tagBlacklistAction, fontSize = 13.sp, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

private fun categoryLabel(category: Int?, strings: AppStrings): String? = when (category) {
    0 -> strings.generalLabel
    1 -> strings.artistLabel
    3 -> strings.copyrightLabel
    4 -> strings.characterLabel
    5 -> strings.tagCategorySpecies
    6 -> strings.tagCategoryInvalid
    7 -> strings.tagCategoryMeta
    8 -> strings.tagCategoryLore
    else -> null
}
