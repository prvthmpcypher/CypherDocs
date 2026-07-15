package com.prvthmpcypher.cypherdocs.util

enum class CypherFileType {
    TEXT,
    IMAGE,
    OFFICE_ZIP_XML, // docx, xlsx, pptx, odt, ods, odp -> extracted as text
    PDF_EXCLUDED,   // recognized but intentionally not handled by this app
    UNKNOWN_BINARY
}

object FileTypeUtils {

    private val TEXT_EXTENSIONS = setOf(
        "txt", "md", "markdown", "json", "xml", "csv", "tsv", "log",
        "java", "kt", "kts", "py", "js", "ts", "html", "htm", "css",
        "ini", "yaml", "yml", "gradle", "properties", "sh", "c", "cpp",
        "h", "hpp", "rs", "go", "rb", "php", "sql", "toml", "gitignore",
        "srt", "vtt"
    )

    private val IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif"
    )

    private val OFFICE_ZIP_EXTENSIONS = setOf(
        "docx", "xlsx", "pptx", "odt", "ods", "odp"
    )

    private val PDF_EXTENSIONS = setOf("pdf")

    fun extensionOf(name: String): String {
        val dot = name.lastIndexOf('.')
        return if (dot >= 0 && dot < name.length - 1) name.substring(dot + 1).lowercase() else ""
    }

    fun classify(name: String, mimeType: String?): CypherFileType {
        val ext = extensionOf(name)
        return when {
            ext in PDF_EXTENSIONS || mimeType == "application/pdf" -> CypherFileType.PDF_EXCLUDED
            ext in IMAGE_EXTENSIONS || (mimeType?.startsWith("image/") == true) -> CypherFileType.IMAGE
            ext in OFFICE_ZIP_EXTENSIONS -> CypherFileType.OFFICE_ZIP_XML
            ext in TEXT_EXTENSIONS || (mimeType?.startsWith("text/") == true) -> CypherFileType.TEXT
            else -> CypherFileType.UNKNOWN_BINARY
        }
    }

    fun isSupportedNonPdf(name: String, mimeType: String?): Boolean {
        return classify(name, mimeType) != CypherFileType.PDF_EXCLUDED
    }
}
