package com.prvthmpcypher.cypherdocs

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prvthmpcypher.cypherdocs.util.RecentFile
import com.prvthmpcypher.cypherdocs.util.RecentFilesStore
import com.prvthmpcypher.cypherdocs.viewer.ViewerRouter

/**
 * CypherDocs has no launcher/MAIN activity, so it never shows up in the app drawer.
 * This is the ONLY exported activity: it exists purely to receive an incoming
 * ACTION_VIEW / ACTION_SEND intent from another app's "Open with…" or share sheet,
 * figure out which viewer the file needs, hand it off, and disappear.
 */
class OpenWithEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = resolveIncomingUri()
        if (uri == null) {
            Toast.makeText(this, getString(R.string.file_open_error), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Best-effort: hang on to read access so this file can be reopened later from Recents.
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // Not every incoming Uri grants a persistable permission (e.g. one-shot SEND grants) - that's fine.
        }

        val mimeType = intent.type ?: contentResolver.getType(uri)
        val name = queryDisplayName(uri) ?: uri.lastPathSegment ?: "file"

        RecentFilesStore.add(
            this,
            RecentFile(
                uri = uri.toString(),
                name = name,
                mimeType = mimeType,
                parentTreeUri = null, // opened externally: no folder context, so no page navigation siblings
                timestamp = System.currentTimeMillis()
            )
        )

        ViewerRouter.open(
            context = this,
            uri = uri,
            name = name,
            mimeType = mimeType,
            siblings = arrayListOf(),
            siblingNames = arrayListOf(),
            index = 0
        )
        finish()
    }

    private fun resolveIncomingUri(): Uri? {
        return when (intent.action) {
            Intent.ACTION_SEND -> getParcelableExtraCompat(Intent.EXTRA_STREAM)
            else -> intent.data
        }
    }

    @Suppress("DEPRECATION")
    private fun getParcelableExtraCompat(key: String): Uri? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(key, Uri::class.java)
        } else {
            intent.getParcelableExtra(key)
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        if (uri.scheme != "content") return uri.lastPathSegment
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }
}
