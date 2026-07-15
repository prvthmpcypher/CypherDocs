package com.prvthmpcypher.cypherdocs.viewer

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.util.FileTypeUtils
import com.prvthmpcypher.cypherdocs.util.CypherFileType

open class TextViewerActivity : BaseViewerActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var textContent: TextView
    private lateinit var searchBar: View
    private lateinit var editSearch: EditText
    private lateinit var matchCountText: TextView

    private var rawText: String = ""
    private var currentFontSize = 15f
    private val matchRanges = mutableListOf<IntRange>()
    private var currentMatchIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId())
        readCommonExtras()

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = fileName
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)

        textContent = findViewById(R.id.text_content)
        searchBar = findViewById(R.id.layout_search_bar)
        editSearch = findViewById(R.id.edit_search)
        matchCountText = findViewById(R.id.text_match_count)

        findViewById<ImageButton>(R.id.button_zoom_in).setOnClickListener { changeFontSize(2f) }
        findViewById<ImageButton>(R.id.button_zoom_out).setOnClickListener { changeFontSize(-2f) }
        findViewById<ImageButton>(R.id.button_search_close).setOnClickListener { closeSearch() }
        findViewById<ImageButton>(R.id.button_search_next).setOnClickListener { jumpToMatch(currentMatchIndex + 1) }
        findViewById<ImageButton>(R.id.button_search_prev).setOnClickListener { jumpToMatch(currentMatchIndex - 1) }

        editSearch.doAfterTextChanged { runSearch(it?.toString().orEmpty()) }

        setupPageNavigation(
            findViewById(R.id.button_prev_file),
            findViewById(R.id.button_next_file),
            findViewById(R.id.text_page_indicator)
        )

        loadContent()
    }

    protected open fun layoutResId() = R.layout.activity_text_viewer

    protected open fun loadContent() {
        try {
            val type = FileTypeUtils.classify(fileName, mimeType)
            rawText = if (type == CypherFileType.UNKNOWN_BINARY) {
                buildBinaryInfoNotice()
            } else {
                contentResolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readText() }
                    ?: getString(R.string.file_open_error)
            }
        } catch (e: Exception) {
            rawText = getString(R.string.file_open_error) + "\n\n${e.message}"
        }
        renderText()
    }

    private fun buildBinaryInfoNotice(): String {
        var size = -1L
        try {
            contentResolver.openInputStream(fileUri)?.use { size = it.available().toLong() }
        } catch (_: Exception) {
        }
        return buildString {
            append(getString(R.string.unsupported_binary_notice))
            append("\n\n")
            append("File: $fileName\n")
            if (size >= 0) append("Approx. size: ${size} bytes\n")
            mimeType?.let { append("Type: $it\n") }
        }
    }

    /** Lets subclasses (e.g. DocViewerActivity) inject already-processed text instead of raw file bytes. */
    protected fun overrideText(text: String) {
        rawText = text
        renderText()
    }

    protected fun renderText() {
        textContent.textSize = currentFontSize
        textContent.text = rawText
    }

    private fun changeFontSize(delta: Float) {
        currentFontSize = (currentFontSize + delta).coerceIn(10f, 32f)
        textContent.textSize = currentFontSize
    }

    private fun runSearch(query: String) {
        matchRanges.clear()
        currentMatchIndex = -1
        if (query.isBlank()) {
            matchCountText.text = ""
            renderText()
            return
        }
        var start = 0
        val lower = rawText.lowercase()
        val q = query.lowercase()
        while (true) {
            val idx = lower.indexOf(q, start)
            if (idx == -1) break
            matchRanges.add(idx until (idx + q.length))
            start = idx + q.length
        }
        matchCountText.text = if (matchRanges.isEmpty()) {
            getString(R.string.search_no_matches)
        } else {
            getString(R.string.search_match_count, 1, matchRanges.size)
        }
        highlightMatches()
        if (matchRanges.isNotEmpty()) jumpToMatch(0)
    }

    private fun highlightMatches() {
        val spannable = SpannableString(rawText)
        matchRanges.forEach { range ->
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                range.first, range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textContent.textSize = currentFontSize
        textContent.text = spannable
    }

    private fun jumpToMatch(index: Int) {
        if (matchRanges.isEmpty()) return
        currentMatchIndex = ((index % matchRanges.size) + matchRanges.size) % matchRanges.size
        matchCountText.text = getString(R.string.search_match_count, currentMatchIndex + 1, matchRanges.size)
        highlightMatches()
        // Best-effort scroll: TextView line lookup for the match start offset.
        val layout = textContent.layout
        if (layout != null) {
            val line = layout.getLineForOffset(matchRanges[currentMatchIndex].first)
            val y = layout.getLineTop(line)
            (textContent.parent as? android.widget.ScrollView)?.smoothScrollTo(0, y)
        }
    }

    private fun closeSearch() {
        searchBar.visibility = View.GONE
        editSearch.setText("")
        matchRanges.clear()
        renderText()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_text_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                searchBar.visibility = if (searchBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                true
            }
            R.id.action_share -> {
                shareCurrentFile()
                true
            }
            R.id.action_browse -> {
                startActivity(android.content.Intent(this, com.prvthmpcypher.cypherdocs.MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
