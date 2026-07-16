package com.prvthmpcypher.cypherdocs.util

import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * docx / xlsx / pptx / odt / ods / odp are all ZIP containers of XML.
 * We don't do full rendering (no external libraries, fully offline, no editing) —
 * instead we extract the readable text content so the document can still be READ.
 *
 * This is intentionally simple: good enough to read the words in a document,
 * not a replacement for Word/Excel/PowerPoint rendering.
 */
object OfficeXmlExtractor {

    // Entries worth reading text out of, in priority/order of appearance.
    private val RELEVANT_ENTRY_PATTERNS = listOf(
        Regex("word/document\\.xml"),                 // docx
        Regex("ppt/slides/slide\\d+\\.xml"),           // pptx
        Regex("xl/sharedStrings\\.xml"),               // xlsx shared strings
        Regex("xl/worksheets/sheet\\d+\\.xml"),        // xlsx sheet cells
        Regex("content\\.xml")                         // odt/ods/odp
    )

    private val TAG_REGEX = Regex("<[^>]+>")
    private val WHITESPACE_COLLAPSE = Regex("[ \\t]+")

    /**
     * Returns extracted plain text, or null if nothing readable was found.
     */
    fun extractText(inputStream: InputStream): String? {
        val chunks = mutableListOf<String>()
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val entryName = entry.name
                if (!entry.isDirectory && RELEVANT_ENTRY_PATTERNS.any { it.containsMatchIn(entryName) }) {
                    val bytes = zip.readBytes()
                    val xml = String(bytes, Charsets.UTF_8)
                    val text = xmlToPlainText(xml)
                    if (text.isNotBlank()) chunks.add(text)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        if (chunks.isEmpty()) return null
        return chunks.joinToString("\n\n---\n\n").trim().ifBlank { null }
    }

    private fun xmlToPlainText(xml: String): String {
        // Convert common block/paragraph/line-break tags into newlines before stripping tags,
        // so the extracted text isn't one giant run-on line.
        val withNewlines = xml
            .replace(Regex("</w:p>"), "\n")          // docx paragraph end
            .replace(Regex("<w:br[^>]*/>"), "\n")     // docx line break
            .replace(Regex("</a:p>"), "\n")           // pptx paragraph end
            .replace(Regex("</text:p>"), "\n")        // odt paragraph end
            .replace(Regex("</row>"), "\n")           // xlsx row end (approx)
            .replace(Regex("</si>"), "\n")            // xlsx shared string entry end

        val noTags = withNewlines.replace(TAG_REGEX, "")
        val decoded = decodeXmlEntities(noTags)

        return decoded
            .lines()
            .map { it.replace(WHITESPACE_COLLAPSE, " ").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    private fun decodeXmlEntities(s: String): String {
        return s
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }
}
