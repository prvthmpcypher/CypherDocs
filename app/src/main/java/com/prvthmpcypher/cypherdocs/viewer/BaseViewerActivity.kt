package com.prvthmpcypher.cypherdocs.viewer

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.util.ShareUtils

abstract class BaseViewerActivity : AppCompatActivity() {

    protected lateinit var fileUri: Uri
    protected lateinit var fileName: String
    protected var mimeType: String? = null

    private var siblings: ArrayList<Uri> = arrayListOf()
    private var siblingNames: ArrayList<String> = arrayListOf()
    private var currentIndex: Int = 0

    protected fun readCommonExtras() {
        fileUri = getUriExtraCompat(ViewerRouter.EXTRA_URI)
            ?: throw IllegalStateException("Missing file uri")
        fileName = intent.getStringExtra(ViewerRouter.EXTRA_NAME) ?: "Untitled"
        mimeType = intent.getStringExtra(ViewerRouter.EXTRA_MIME)
        siblings = getUriArrayListExtraCompat(ViewerRouter.EXTRA_SIBLINGS)
        siblingNames = intent.getStringArrayListExtra(ViewerRouter.EXTRA_SIBLING_NAMES) ?: arrayListOf()
        currentIndex = intent.getIntExtra(ViewerRouter.EXTRA_INDEX, 0)
    }

    @Suppress("DEPRECATION")
    private fun getUriExtraCompat(key: String): Uri? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(key, Uri::class.java)
        } else {
            intent.getParcelableExtra(key)
        }
    }

    @Suppress("DEPRECATION")
    private fun getUriArrayListExtraCompat(key: String): ArrayList<Uri> {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableArrayListExtra(key, Uri::class.java) ?: arrayListOf()
        } else {
            intent.getParcelableArrayListExtra(key) ?: arrayListOf()
        }
    }

    /** Wires up bottom-bar next/prev buttons + page indicator text to sibling navigation. */
    protected fun setupPageNavigation(prevButton: ImageButton, nextButton: ImageButton, indicator: TextView) {
        val hasFolder = siblings.isNotEmpty()
        indicator.text = if (hasFolder) {
            getString(R.string.page_indicator, currentIndex + 1, siblings.size)
        } else {
            ""
        }

        prevButton.isEnabled = hasFolder && currentIndex > 0
        nextButton.isEnabled = hasFolder && currentIndex < siblings.size - 1
        prevButton.alpha = if (prevButton.isEnabled) 1f else 0.35f
        nextButton.alpha = if (nextButton.isEnabled) 1f else 0.35f

        prevButton.setOnClickListener { navigateTo(currentIndex - 1) }
        nextButton.setOnClickListener { navigateTo(currentIndex + 1) }
    }

    private fun navigateTo(newIndex: Int) {
        if (newIndex < 0 || newIndex >= siblings.size) return
        val nextUri = siblings[newIndex]
        val nextName = siblingNames.getOrElse(newIndex) { nextUri.lastPathSegment ?: "file" }
        ViewerRouter.open(
            context = this,
            uri = nextUri,
            name = nextName,
            mimeType = null, // let router re-classify by extension
            siblings = siblings,
            siblingNames = siblingNames,
            index = newIndex
        )
        finish()
    }

    protected fun shareCurrentFile() {
        ShareUtils.shareFile(this, fileUri, fileName, mimeType)
    }
}
