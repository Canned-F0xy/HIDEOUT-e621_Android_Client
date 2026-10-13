package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.CannedF0xy.hideout.i18n.LocalStrings

@Composable
fun SelectionActionBar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDownload: () -> Unit,
    onAddToCollection: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        shadowElevation = 12.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                strings.selectedCount(selectedCount),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = strings.selectAll, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onAddToCollection) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = strings.collectionPickerTitle, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = strings.cdDownload, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
