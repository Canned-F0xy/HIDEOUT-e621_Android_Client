package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.SavedSearch
import com.CannedF0xy.hideout.i18n.LocalStrings

private data class FilterChipSpec(val label: String, val token: String)

object SearchFilterTokens {
    fun apply(text: String, token: String): String {
        val prefix = token.substringBefore(":") + ":"
        val words = text.split(" ").filter { it.isNotBlank() }
        val alreadyActive = words.contains(token)
        val kept = words.filter { !it.startsWith(prefix) }
        val result = if (alreadyActive) kept else kept + token
        return if (result.isEmpty()) "" else result.joinToString(" ") + " "
    }

    fun isActive(text: String, token: String): Boolean = text.split(" ").contains(token)
}

@Composable
fun SearchAssistPanel(
    searchText: String,
    history: List<String>,
    isNsfwEnabled: Boolean,
    onApplyFilter: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onHistoryRemove: (String) -> Unit,
    onHistoryClear: () -> Unit,
    savedSearches: List<SavedSearch> = emptyList(),
    onSavedSearchClick: (String) -> Unit = {}
) {
    val strings = LocalStrings.current
    val chips = buildList {
        add(FilterChipSpec(strings.filterOrderScore, "order:score"))
        add(FilterChipSpec(strings.filterOrderFavcount, "order:favcount"))
        add(FilterChipSpec(strings.filterOrderRandom, "order:random"))
        add(FilterChipSpec(strings.filterOrderOldest, "order:id_asc"))
        add(FilterChipSpec(strings.filterRatingSafe, "rating:s"))
        if (isNsfwEnabled) {
            add(FilterChipSpec(strings.filterRatingQuestionable, "rating:q"))
            add(FilterChipSpec(strings.filterRatingExplicit, "rating:e"))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            if (savedSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedSearches.forEach { saved ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(50))
                                .clickable { onSavedSearchClick(saved.query) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("📌 ${saved.name}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip ->
                    val active = SearchFilterTokens.isActive(searchText, chip.token)
                    Box(
                        modifier = Modifier
                            .background(
                                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(50)
                            )
                            .clickable { onApplyFilter(SearchFilterTokens.apply(searchText, chip.token)) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            chip.label,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (history.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.searchHistoryTitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = onHistoryClear) {
                        Text(strings.clearAll, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(history) { query ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onHistoryClick(query) }.padding(start = 20.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Text(
                                query,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(start = 12.dp)
                            )
                            IconButton(onClick = { onHistoryRemove(query) }) {
                                Icon(Icons.Default.Close, contentDescription = strings.cdDelete, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
