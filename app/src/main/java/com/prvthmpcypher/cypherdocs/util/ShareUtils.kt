package com.prvthmpcypher.cypherdocs.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {

    /**
     * Copies the given source content Uri into our own FileProvider-exposed cache directory
     * (required because arbitrary SAF Uris usually aren't shareable to other apps directly),
     * then launches the system Share sheet for it.
     */
    fun shareFile(context: Context, sourceUri: Uri, displayName: String, mimeType: String?) {
        try {
            val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
            val outFile = File(sharedDir, displayName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, outFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share \"$displayName\""))
        } catch (e: Exception) {
            // Fail quietly with a toast at the call site if needed; sharing is a nice-to-have.
        }
    }
}
