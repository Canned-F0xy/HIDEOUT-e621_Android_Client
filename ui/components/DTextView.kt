package com.CannedF0xy.hideout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.ui.theme.ContrastUtils
import com.CannedF0xy.hideout.ui.theme.LocalCustomThemeEnabled
import com.CannedF0xy.hideout.util.DTextBlock
import com.CannedF0xy.hideout.util.DTextLink
import com.CannedF0xy.hideout.util.DTextParser
import com.CannedF0xy.hideout.util.DTextSegment

private val categoryColors = mapOf(
    "artist" to Color(0xFFFF5252),
    "copyright" to Color(0xFFE040FB),
    "character" to Color(0xFF69F0AE),
    "general" to Color(0xFF40C4FF),
    "species" to Color(0xFFED5D1F),
    "meta" to Color(0xFFBDBDBD),
    "lore" to Color(0xFF228822),
    "invalid" to Color(0xFFFF3D3D),
    "contributor" to Color(0xFFC0C0C0)
)

private class DTextPalette(
    val text: Color,
    val muted: Color,
    val link: Color,
    val accent: Color,
    val codeBackground: Color,
    val spoilerHidden: Color,
    val adjust: (Color) -> Color
)

@Composable
fun DTextView(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = 14.sp,
    background: Color = MaterialTheme.colorScheme.background,
    onSearch: ((String) -> Unit)? = null
) {
    val blocks = remember(text) { runCatching { DTextParser.parse(text) }.getOrElse { listOf(DTextBlock.Paragraph(listOf(DTextSegment(text)))) } }
    val uriHandler = LocalUriHandler.current
    val customTheme = LocalCustomThemeEnabled.current
    val scheme = MaterialTheme.colorScheme
    val effectiveBackground = if (background.alpha < 1f) {
        androidx.compose.ui.graphics.lerp(scheme.background, background.copy(alpha = 1f), background.alpha)
    } else background
    val adjust: (Color) -> Color = { c -> if (customTheme) ContrastUtils.ensureContrast(c, listOf(effectiveBackground), 4.5f) else c }
    val palette = DTextPalette(
        text = color,
        muted = scheme.onSurfaceVariant,
        link = adjust(Color(0xFF40C4FF)),
        accent = scheme.primary,
        codeBackground = color.copy(alpha = 0.08f),
        spoilerHidden = color,
        adjust = adjust
    )
    var revealedSpoilers by remember(text) { mutableStateOf(emptySet<Int>()) }

    val openLink: (DTextLink) -> Unit = { link ->
        when (link) {
            is DTextLink.Search -> {
                if (onSearch != null) onSearch(link.query)
                else runCatching { uriHandler.openUri("https://e621.net/posts?tags=" + java.net.URLEncoder.encode(link.query, "UTF-8")) }
            }
            is DTextLink.Url -> {
                val internal = DTextParser.e621Target(link.url)
                if (internal != null && onSearch != null) onSearch(internal.query)
                else runCatching { uriHandler.openUri(link.url) }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DTextBlocks(
            blocks = blocks,
            palette = palette,
            fontSize = fontSize,
            revealed = revealedSpoilers,
            onToggleSpoiler = { id -> revealedSpoilers = if (id in revealedSpoilers) revealedSpoilers - id else revealedSpoilers + id },
            onLink = openLink
        )
    }
}

@Composable
private fun DTextBlocks(
    blocks: List<DTextBlock>,
    palette: DTextPalette,
    fontSize: TextUnit,
    revealed: Set<Int>,
    onToggleSpoiler: (Int) -> Unit,
    onLink: (DTextLink) -> Unit
) {
    val strings = LocalStrings.current
    blocks.forEach { block ->
        when (block) {
            is DTextBlock.Paragraph -> Text(
                buildSegments(block.segments, palette, revealed, onToggleSpoiler, onLink),
                color = palette.text,
                fontSize = fontSize
            )
            is DTextBlock.Header -> {
                val size = when (block.level) {
                    1 -> 22.sp
                    2 -> 20.sp
                    3 -> 18.sp
                    4 -> 16.sp
                    5 -> 15.sp
                    else -> 14.sp
                }
                Text(
                    buildSegments(block.segments, palette, revealed, onToggleSpoiler, onLink),
                    color = palette.text,
                    fontSize = size,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = if (block.level <= 2) 4.dp else 2.dp)
                )
            }
            is DTextBlock.ListItem -> Row(modifier = Modifier.padding(start = (12 * (block.depth - 1)).dp)) {
                Text(if (block.depth == 1) "•" else "◦", color = palette.accent, fontSize = fontSize, modifier = Modifier.padding(end = 8.dp))
                Text(
                    buildSegments(block.segments, palette, revealed, onToggleSpoiler, onLink),
                    color = palette.text,
                    fontSize = fontSize
                )
            }
            is DTextBlock.Quote -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.text.copy(alpha = 0.05f))
            ) {
                Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(palette.accent.copy(alpha = 0.7f)))
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DTextBlocks(block.children, palette, fontSize, revealed, onToggleSpoiler, onLink)
                }
            }
            is DTextBlock.Code -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.codeBackground)
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Text(block.text, color = palette.text, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
            is DTextBlock.Section -> {
                var expanded by rememberSaveable(block.title, block.children.size) { mutableStateOf(block.expanded) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.text.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = palette.accent
                        )
                        Text(block.title ?: strings.dtextSectionDefault, color = palette.text, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
                    }
                    if (expanded) {
                        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DTextBlocks(block.children, palette, fontSize, revealed, onToggleSpoiler, onLink)
                        }
                    }
                }
            }
            is DTextBlock.Spoiler -> {
                var shown by remember(block) { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.text.copy(alpha = if (shown) 0.05f else 0.12f))
                        .clickable { shown = !shown }
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (shown) {
                        DTextBlocks(block.children, palette, fontSize, revealed, onToggleSpoiler, onLink)
                    } else {
                        Text(strings.dtextSpoilerHint, color = palette.muted, fontSize = 13.sp, fontStyle = FontStyle.Italic)
                    }
                }
            }
            DTextBlock.Divider -> Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.text.copy(alpha = 0.15f)))
        }
    }
}

private fun resolveColor(value: String?, palette: DTextPalette): Color? {
    if (value.isNullOrBlank()) return null
    val key = value.trim().lowercase()
    categoryColors[key]?.let { return palette.adjust(it) }
    val parsed = runCatching {
        val normalized = if (Regex("^#?[0-9a-f]{3}$").matches(key)) {
            "#" + key.removePrefix("#").map { "$it$it" }.joinToString("")
        } else if (Regex("^[0-9a-f]{6}$").matches(key)) "#$key" else key
        Color(android.graphics.Color.parseColor(normalized))
    }.getOrNull() ?: return null
    return palette.adjust(parsed)
}

private fun buildSegments(
    segments: List<DTextSegment>,
    palette: DTextPalette,
    revealed: Set<Int>,
    onToggleSpoiler: (Int) -> Unit,
    onLink: (DTextLink) -> Unit
): AnnotatedString = buildAnnotatedString {
    segments.forEach { segment ->
        val hidden = segment.spoilerId != null && segment.spoilerId !in revealed
        val decorations = buildList {
            if (segment.underline || segment.link != null) add(TextDecoration.Underline)
            if (segment.strike) add(TextDecoration.LineThrough)
        }
        val baseColor = when {
            hidden -> palette.spoilerHidden
            segment.link != null -> palette.link
            else -> resolveColor(segment.color, palette)
        }
        val style = SpanStyle(
            color = baseColor ?: Color.Unspecified,
            fontWeight = if (segment.bold) FontWeight.Bold else null,
            fontStyle = if (segment.italic) FontStyle.Italic else null,
            fontFamily = if (segment.code) FontFamily.Monospace else null,
            fontSize = when {
                segment.superscript || segment.subscript -> 0.75.em
                segment.small -> 0.85.em
                else -> TextUnit.Unspecified
            },
            baselineShift = when {
                segment.superscript -> BaselineShift.Superscript
                segment.subscript -> BaselineShift.Subscript
                else -> null
            },
            background = when {
                hidden -> palette.spoilerHidden
                segment.spoilerId != null -> palette.text.copy(alpha = 0.08f)
                segment.code -> palette.codeBackground
                else -> Color.Unspecified
            },
            textDecoration = if (decorations.isEmpty() || hidden) null else TextDecoration.combine(decorations)
        )
        val spoilerId = segment.spoilerId
        val link = segment.link
        when {
            spoilerId != null && hidden -> withLink(LinkAnnotation.Clickable("spoiler-$spoilerId", TextLinkStyles(style)) { onToggleSpoiler(spoilerId) }) {
                append(segment.text)
            }
            link != null -> withLink(LinkAnnotation.Clickable("link", TextLinkStyles(style)) { onLink(link) }) {
                append(segment.text)
            }
            else -> withStyle(style) { append(segment.text) }
        }
    }
}
