package com.CannedF0xy.hideout.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagSection(
    title: String,
    tags: List<String>,
    color: Color,
    onTagClick: (String) -> Unit,
    onTagLongClick: ((String) -> Unit)? = null
) {
    if (tags.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.2f)).combinedClickable(onClick = { onTagClick(tag) }, onLongClick = onTagLongClick?.let { handler -> { handler(tag) } }).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(tag, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
