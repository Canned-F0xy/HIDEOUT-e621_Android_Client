package com.CannedF0xy.hideout.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.network.model.Comment
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CommentsSection(postId: Int) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var comments by remember(postId) { mutableStateOf<List<Comment>?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isExpanded = !isExpanded
                        if (isExpanded && comments == null) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val resStr = NetworkModule.api!!.getComments(postId).string()
                                    val element = com.google.gson.JsonParser().parse(resStr)
                                    comments = if (element.isJsonArray) {
                                        Gson().fromJson(element, Array<Comment>::class.java).toList()
                                    } else if (element.isJsonObject && element.asJsonObject.has("comments")) {
                                        Gson().fromJson(element.asJsonObject.get("comments"), Array<Comment>::class.java).toList()
                                    } else {
                                        emptyList()
                                    }
                                } catch (e: Exception) {
                                    comments = emptyList()
                                    withContext(Dispatchers.Main) { Toast.makeText(context, strings.loadFailed, Toast.LENGTH_SHORT).show() }
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.commentsTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    if (isLoading) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) {
                                Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                            }
                        }
                    } else if (comments.isNullOrEmpty()) {
                        Text(strings.noComments, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp), fontSize = 13.sp)
                    } else {
                        comments!!.forEach { comment ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(comment.creator_name ?: "Unknown", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                        Text("⭐ ${comment.score}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comment.body ?: "", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
