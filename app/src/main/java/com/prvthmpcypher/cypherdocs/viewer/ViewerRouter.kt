package com.prvthmpcypher.cypherdocs.viewer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.prvthmpcypher.cypherdocs.util.CypherFileType
import com.prvthmpcypher.cypherdocs.util.FileTypeUtils

object ViewerRouter {

    const val EXTRA_URI = "extra_uri"
    const val EXTRA_NAME = "extra_name"
    const val EXTRA_MIME = "extra_mime"
    const val EXTRA_SIBLINGS = "extra_siblings"
    const val EXTRA_SIBLING_NAMES = "extra_sibling_names"
    const val EXTRA_INDEX = "extra_index"

    /**
     * @param siblings other file Uris in the same folder (for next/prev page navigation).
     *                 Pass an empty list if there's no folder context (e.g. a one-off file open).
     */
    fun open(
        context: Context,
        uri: Uri,
        name: String,
        mimeType: String?,
        siblings: ArrayList<Uri> = arrayListOf(),
        siblingNames: ArrayList<String> = arrayListOf(),
        index: Int = 0
    ) {
        val type = FileTypeUtils.classify(name, mimeType)

        val targetClass = when (type) {
            CypherFileType.PDF_EXCLUDED -> {
                Toast.makeText(
                    context,
                    "CypherDocs intentionally doesn't open PDFs — use CypherPDF for those.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            CypherFileType.IMAGE -> ImageViewerActivity::class.java
            CypherFileType.OFFICE_ZIP_XML -> DocViewerActivity::class.java
            CypherFileType.TEXT, CypherFileType.UNKNOWN_BINARY -> TextViewerActivity::class.java
        }

        val intent = Intent(context, targetClass).apply {
            putExtra(EXTRA_URI, uri)
            putExtra(EXTRA_NAME, name)
            putExtra(EXTRA_MIME, mimeType)
            putParcelableArrayListExtra(EXTRA_SIBLINGS, siblings)
            putStringArrayListExtra(EXTRA_SIBLING_NAMES, siblingNames)
            putExtra(EXTRA_INDEX, index)
        }
        context.startActivity(intent)
    }
}
