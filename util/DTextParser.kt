package com.CannedF0xy.hideout.util

sealed class DTextLink {
    data class Url(val url: String) : DTextLink()
    data class Search(val query: String) : DTextLink()
}

data class DTextSegment(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strike: Boolean = false,
    val superscript: Boolean = false,
    val subscript: Boolean = false,
    val small: Boolean = false,
    val code: Boolean = false,
    val color: String? = null,
    val spoilerId: Int? = null,
    val link: DTextLink? = null
)

sealed class DTextBlock {
    data class Paragraph(val segments: List<DTextSegment>) : DTextBlock()
    data class Header(val level: Int, val segments: List<DTextSegment>) : DTextBlock()
    data class ListItem(val depth: Int, val segments: List<DTextSegment>) : DTextBlock()
    data class Quote(val children: List<DTextBlock>) : DTextBlock()
    data class Code(val text: String) : DTextBlock()
    data class Section(val title: String?, val expanded: Boolean, val children: List<DTextBlock>) : DTextBlock()
    data class Spoiler(val children: List<DTextBlock>) : DTextBlock()
    object Divider : DTextBlock()
}

object DTextParser {
    private const val SITE = "https://e621.net"

    private val blockTagRegex = Regex("""\[(/?)(quote|code|section|spoiler|spoilers)((?:[=,][^\]\n]*)?)\]""", RegexOption.IGNORE_CASE)
    private val headerRegex = Regex("""^\s*h([1-6])(?:#[\w\-]+)?\.\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val listRegex = Regex("""^\s*(\*+)\s+(.*)$""")
    private val dividerRegex = Regex("""^\s*\[hr\]\s*$""", RegexOption.IGNORE_CASE)
    private val codeCloseRegex = Regex("""\[/code\]""", RegexOption.IGNORE_CASE)
    private val spoilerTagRegex = Regex("""\[(/?)(spoiler|spoilers)(?:=[^\]]*)?\]""", RegexOption.IGNORE_CASE)

    private val inlineRegex = Regex(
        listOf(
            """\[(/?)(b|i|u|s|sup|sub|tn|spoiler|spoilers|color)(=[^\]]*)?\]""",
            """"([^"\n]+)":\[([^\]\s]+)\]""",
            """"([^"\n]+)":((?:https?://|/)[^\s<>"\[\]]+)""",
            """<(https?://[^\s>]+)>""",
            """\[\[([^\]|\n]+)(?:\|([^\]\n]+))?\]\]""",
            """\{\{([^\}|\n]+)(?:\|([^\}\n]+))?\}\}""",
            """\b(post|pool|user|comment|forum|topic|set|wiki|note|ticket|record|takedown|artist|blip|mod action)\s#(\d+)""",
            """`([^`\n]+)`""",
            """https?://[^\s<>"\[\]]+"""
        ).joinToString("|") { "($it)" },
        RegexOption.IGNORE_CASE
    )

    private val idPaths = mapOf(
        "user" to "users",
        "comment" to "comments",
        "forum" to "forum_posts",
        "topic" to "forum_topics",
        "set" to "post_sets",
        "wiki" to "wiki_pages",
        "note" to "notes",
        "ticket" to "tickets",
        "record" to "user_feedbacks",
        "takedown" to "takedowns",
        "artist" to "artists",
        "blip" to "blips",
        "mod action" to "mod_actions"
    )

    fun parse(raw: String): List<DTextBlock> = try {
        val normalized = normalizeHtml(raw.replace("\r\n", "\n").replace('\r', '\n'))
        val spoilerCounter = intArrayOf(0)
        parseBlocks(normalized, spoilerCounter).blocks
    } catch (e: Throwable) {
        listOf(DTextBlock.Paragraph(listOf(DTextSegment(raw))))
    }

    private data class BlockResult(val blocks: List<DTextBlock>, val end: Int)

    private fun parseBlocks(text: String, spoilerCounter: IntArray, start: Int = 0, closingTag: String? = null): BlockResult {
        val blocks = mutableListOf<DTextBlock>()
        var cursor = start
        var textStart = start
        while (cursor < text.length) {
            val match = blockTagRegex.find(text, cursor) ?: break
            val isClose = match.groupValues[1] == "/"
            val name = normalizeTagName(match.groupValues[2])
            val argument = match.groupValues[3]
            if (isClose) {
                if (closingTag != null && name == closingTag) {
                    blocks += parseLines(text.substring(textStart, match.range.first), spoilerCounter)
                    return BlockResult(blocks, match.range.last + 1)
                }
                cursor = match.range.last + 1
                continue
            }
            when (name) {
                "code" -> {
                    val close = codeCloseRegex.find(text, match.range.last + 1)
                    blocks += parseLines(text.substring(textStart, match.range.first), spoilerCounter)
                    val codeEnd = close?.range?.first ?: text.length
                    blocks += DTextBlock.Code(text.substring(match.range.last + 1, codeEnd).trim('\n'))
                    cursor = close?.range?.last?.plus(1) ?: text.length
                    textStart = cursor
                }
                "spoiler" -> {
                    val closeIndex = findClosing(text, match.range.last + 1)
                    val inner = if (closeIndex >= 0) text.substring(match.range.last + 1, closeIndex) else ""
                    if (closeIndex >= 0 && inner.contains('\n')) {
                        blocks += parseLines(text.substring(textStart, match.range.first), spoilerCounter)
                        val result = parseBlocks(text, spoilerCounter, match.range.last + 1, "spoiler")
                        blocks += DTextBlock.Spoiler(result.blocks)
                        cursor = result.end
                        textStart = cursor
                    } else {
                        cursor = match.range.last + 1
                    }
                }
                else -> {
                    blocks += parseLines(text.substring(textStart, match.range.first), spoilerCounter)
                    val result = parseBlocks(text, spoilerCounter, match.range.last + 1, name)
                    blocks += if (name == "quote") {
                        DTextBlock.Quote(result.blocks)
                    } else {
                        val expanded = argument.startsWith(",expanded", ignoreCase = true)
                        val title = argument.substringAfter('=', "").trim().ifBlank { null }
                        DTextBlock.Section(title, expanded, result.blocks)
                    }
                    cursor = result.end
                    textStart = cursor
                }
            }
        }
        blocks += parseLines(text.substring(textStart.coerceAtMost(text.length)), spoilerCounter)
        return BlockResult(blocks, text.length)
    }

    private fun normalizeTagName(name: String): String {
        val lower = name.lowercase()
        return if (lower == "spoilers") "spoiler" else lower
    }

    private fun findClosing(text: String, from: Int): Int {
        val regex = spoilerTagRegex
        var depth = 1
        var index = from
        while (true) {
            val match = regex.find(text, index) ?: return -1
            if (match.groupValues[1] == "/") {
                depth--
                if (depth == 0) return match.range.first
            } else {
                depth++
            }
            index = match.range.last + 1
        }
    }

    private fun parseLines(chunk: String, spoilerCounter: IntArray): List<DTextBlock> {
        if (chunk.isBlank()) return emptyList()
        val blocks = mutableListOf<DTextBlock>()
        val paragraph = StringBuilder()

        fun flush() {
            val content = paragraph.toString().trim('\n', ' ')
            if (content.isNotBlank()) blocks += DTextBlock.Paragraph(parseInline(content, spoilerCounter))
            paragraph.clear()
        }

        for (line in chunk.split('\n')) {
            val header = headerRegex.find(line)
            val listItem = listRegex.find(line)
            when {
                header != null -> {
                    flush()
                    blocks += DTextBlock.Header(header.groupValues[1].toInt(), parseInline(header.groupValues[2].trim(), spoilerCounter))
                }
                dividerRegex.matches(line) -> {
                    flush()
                    blocks += DTextBlock.Divider
                }
                listItem != null -> {
                    flush()
                    blocks += DTextBlock.ListItem(listItem.groupValues[1].length, parseInline(listItem.groupValues[2].trim(), spoilerCounter))
                }
                line.isBlank() -> flush()
                else -> {
                    if (paragraph.isNotEmpty()) paragraph.append('\n')
                    paragraph.append(line)
                }
            }
        }
        flush()
        return blocks
    }

    private data class OpenTag(val name: String, val argument: String?, val spoilerId: Int?)

    fun parseInline(text: String, spoilerCounter: IntArray = intArrayOf(0)): List<DTextSegment> {
        val segments = mutableListOf<DTextSegment>()
        val stack = mutableListOf<OpenTag>()

        fun style(text: String, link: DTextLink? = null, code: Boolean = false): DTextSegment {
            var segment = DTextSegment(text = text, link = link, code = code)
            for (tag in stack) {
                segment = when (tag.name) {
                    "b" -> segment.copy(bold = true)
                    "i" -> segment.copy(italic = true)
                    "u" -> segment.copy(underline = true)
                    "s" -> segment.copy(strike = true)
                    "sup" -> segment.copy(superscript = true)
                    "sub" -> segment.copy(subscript = true)
                    "tn" -> segment.copy(small = true)
                    "color" -> segment.copy(color = tag.argument)
                    "spoiler" -> segment.copy(spoilerId = tag.spoilerId)
                    else -> segment
                }
            }
            return segment
        }

        fun appendText(value: String) {
            if (value.isEmpty()) return
            val last = segments.lastOrNull()
            val next = style(value)
            if (last != null && last.copy(text = "") == next.copy(text = "")) {
                segments[segments.lastIndex] = last.copy(text = last.text + value)
            } else {
                segments += next
            }
        }

        var cursor = 0
        for (match in inlineRegex.findAll(text)) {
            if (match.range.first < cursor) continue
            appendText(text.substring(cursor, match.range.first))
            val groups = match.groups
            val whole = match.value
            when {
                groups[1] != null -> {
                    val isClose = groups[2]?.value == "/"
                    val name = normalizeTagName(groups[3]!!.value)
                    if (isClose) {
                        val index = stack.indexOfLast { it.name == name }
                        if (index >= 0) stack.removeAt(index) else appendText(whole)
                    } else {
                        val argument = groups[4]?.value?.removePrefix("=")?.trim()
                        val spoilerId = if (name == "spoiler") spoilerCounter[0]++ else null
                        stack += OpenTag(name, argument, spoilerId)
                    }
                }
                groups[5] != null -> segments += style(groups[6]!!.value, DTextLink.Url(absoluteUrl(groups[7]!!.value)))
                groups[8] != null -> {
                    val (url, trailing) = trimTrailing(groups[10]!!.value)
                    segments += style(groups[9]!!.value, DTextLink.Url(absoluteUrl(url)))
                    appendText(trailing)
                }
                groups[11] != null -> segments += style(groups[12]!!.value, DTextLink.Url(groups[12]!!.value))
                groups[13] != null -> {
                    val page = groups[14]!!.value.trim()
                    val label = groups[15]?.value?.trim() ?: page.substringBefore('#')
                    val tag = page.substringBefore('#').trim().replace(' ', '_').lowercase()
                    segments += style(label, DTextLink.Search(tag))
                }
                groups[16] != null -> {
                    val query = groups[17]!!.value.trim()
                    val label = groups[18]?.value?.trim() ?: query
                    segments += style(label, DTextLink.Search(query))
                }
                groups[19] != null -> {
                    val kind = groups[20]!!.value.lowercase()
                    val id = groups[21]!!.value
                    val link = when (kind) {
                        "post" -> DTextLink.Search("id:$id")
                        "pool" -> DTextLink.Search("pool:$id")
                        else -> DTextLink.Url("$SITE/${idPaths[kind] ?: kind}/$id")
                    }
                    segments += style(whole, link)
                }
                groups[22] != null -> segments += style(groups[23]!!.value, code = true)
                groups[24] != null -> {
                    val (url, trailing) = trimTrailing(whole)
                    segments += style(url, DTextLink.Url(url))
                    appendText(trailing)
                }
                else -> appendText(whole)
            }
            cursor = match.range.last + 1
        }
        appendText(text.substring(cursor))
        return segments
    }

    private fun trimTrailing(url: String): Pair<String, String> {
        var end = url.length
        while (end > 0 && url[end - 1] in ".,!?:;)'") {
            if (url[end - 1] == ')' && url.count { it == '(' } >= url.count { it == ')' }) break
            end--
        }
        return url.substring(0, end) to url.substring(end)
    }

    private fun absoluteUrl(url: String): String = if (url.startsWith("/")) SITE + url else url

    fun e621Target(url: String): DTextLink.Search? {
        val match = Regex("""^https?://(?:www\.)?(?:e621|e926)\.net/(posts|pools)/(\d+)(?:[/?#].*)?$""", RegexOption.IGNORE_CASE).find(url) ?: return null
        val id = match.groupValues[2]
        return if (match.groupValues[1].equals("posts", true)) DTextLink.Search("id:$id") else DTextLink.Search("pool:$id")
    }

    private val htmlReplacements: List<Pair<Regex, String>> = listOf(
        Regex("""<h([1-6])[^>]*>\s*""", RegexOption.IGNORE_CASE) to "\nh$1. ",
        Regex("""\s*</h[1-6]\s*>""", RegexOption.IGNORE_CASE) to "\n",
        Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE) to "\n",
        Regex("""<hr\s*/?>""", RegexOption.IGNORE_CASE) to "\n[hr]\n",
        Regex("""</?p(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "\n\n",
        Regex("""<(b|strong)(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "[b]",
        Regex("""</(b|strong)\s*>""", RegexOption.IGNORE_CASE) to "[/b]",
        Regex("""<(i|em)(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "[i]",
        Regex("""</(i|em)\s*>""", RegexOption.IGNORE_CASE) to "[/i]",
        Regex("""<(u|ins)(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "[u]",
        Regex("""</(u|ins)\s*>""", RegexOption.IGNORE_CASE) to "[/u]",
        Regex("""<(s|del|strike)(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "[s]",
        Regex("""</(s|del|strike)\s*>""", RegexOption.IGNORE_CASE) to "[/s]",
        Regex("""<(sup|sub)(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "[$1]",
        Regex("""</(sup|sub)\s*>""", RegexOption.IGNORE_CASE) to "[/$1]",
        Regex("""<blockquote(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "\n[quote]\n",
        Regex("""</blockquote\s*>""", RegexOption.IGNORE_CASE) to "\n[/quote]\n",
        Regex("""<pre(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to "\n[code]\n",
        Regex("""</pre\s*>""", RegexOption.IGNORE_CASE) to "\n[/code]\n",
        Regex("""<li(\s[^>]*)?>\s*""", RegexOption.IGNORE_CASE) to "\n* ",
        Regex("""</li\s*>""", RegexOption.IGNORE_CASE) to "\n",
        Regex("""<a\s[^>]*href\s*=\s*["']([^"']+)["'][^>]*>(.*?)</a\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) to "\"$2\":[$1]",
        Regex("""</?(ul|ol|div|span|font|center|small|big|code|table|tbody|thead|tr|td|th)(\s[^>]*)?>""", RegexOption.IGNORE_CASE) to ""
    )

    private fun normalizeHtml(text: String): String {
        var result = text
        if (Regex("""</?[a-zA-Z][a-zA-Z0-9]*(\s[^<>]*)?/?>""").containsMatchIn(result)) {
            for ((regex, replacement) in htmlReplacements) result = result.replace(regex, replacement)
        }
        return result
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
    }
}
