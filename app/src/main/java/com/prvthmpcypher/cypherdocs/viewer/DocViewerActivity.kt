package com.prvthmpcypher.cypherdocs.viewer

import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.util.OfficeXmlExtractor

class DocViewerActivity : TextViewerActivity() {

    override fun loadContent() {
        val extracted = try {
            contentResolver.openInputStream(fileUri)?.use { OfficeXmlExtractor.extractText(it) }
        } catch (e: Exception) {
            null
        }

        overrideText(
            if (extracted != null) {
                getString(R.string.office_extract_notice) + "\n\n" + extracted
            } else {
                getString(R.string.office_extract_failed)
            }
        )
    }
}
