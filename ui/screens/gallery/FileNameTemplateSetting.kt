package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.download.FileNameFormatter
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.model.FileData
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.network.model.ScoreData
import com.CannedF0xy.hideout.network.model.TagsData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val SAMPLE_POST = Post(
    id = 1234567,
    file = FileData(url = null, ext = "png", md5 = "d41d8cd98f00b204"),
    preview = null,
    sample = null,
    tags = TagsData(
        artist = listOf("artist_name"),
        character = listOf("character_name"),
        copyright = listOf("series_name"),
        species = listOf("fox")
    ),
    score = ScoreData(total = 42),
    description = null,
    relationships = null,
    rating = "s",
    created_at = "2026-10-12T00:00:00.000-04:00"
)

@Composable
fun FileNameTemplateSetting(appPrefs: AppPreferences) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf(FileNameFormatter.DEFAULT_TEMPLATE) }

    LaunchedEffect(Unit) {
        text = appPrefs.fileNameTemplate.first()
    }

    fun update(value: String) {
        text = value
        scope.launch { appPrefs.setFileNameTemplate(value) }
    }

    Column {
        Spacer(Modifier.height(16.dp))
        Text(strings.fileNameTemplateLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { update(it) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(strings.fileNameTemplateHint) }
        )
        Text(
            strings.fileNameTemplatePreview(FileNameFormatter.format(text, SAMPLE_POST, "png")),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.height(8.dp))
        ChipRow(items = FileNameFormatter.TOKENS) { token -> update(text + token) }
        Spacer(Modifier.height(6.dp))
        ChipRow(items = FileNameFormatter.PRESETS) { preset -> update(preset) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { update(FileNameFormatter.DEFAULT_TEMPLATE) }) {
                Text(strings.fileNameTemplateReset, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ChipRow(items: List<String>, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                    .clickable { onClick(item) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
