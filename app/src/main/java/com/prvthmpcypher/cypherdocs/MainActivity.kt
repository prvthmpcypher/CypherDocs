package com.prvthmpcypher.cypherdocs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prvthmpcypher.cypherdocs.ui.FolderBrowserActivity
import com.prvthmpcypher.cypherdocs.ui.RecentFilesAdapter
import com.prvthmpcypher.cypherdocs.util.RecentFile
import com.prvthmpcypher.cypherdocs.util.RecentFilesStore
import com.prvthmpcypher.cypherdocs.viewer.ViewerRouter

class MainActivity : AppCompatActivity() {

    private val openTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                val name = DocumentFile.fromTreeUri(this, uri)?.name ?: "Folder"
                val intent = Intent(this, FolderBrowserActivity::class.java).apply {
                    putExtra(FolderBrowserActivity.EXTRA_TREE_URI, uri)
                    putExtra(FolderBrowserActivity.EXTRA_TITLE, name)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<Button>(R.id.button_open_file).setOnClickListener {
            openTreeLauncher.launch(null)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRecents()
    }

    private fun refreshRecents() {
        val recents = RecentFilesStore.loadAll(this)
        val recycler = findViewById<RecyclerView>(R.id.recycler_recent_files)
        val recentTitle = findViewById<TextView>(R.id.text_recent_title)
        val emptyState = findViewById<View>(R.id.layout_empty_state)

        recentTitle.visibility = if (recents.isEmpty()) View.GONE else View.VISIBLE
        emptyState.visibility = if (recents.isEmpty()) View.VISIBLE else View.GONE

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = RecentFilesAdapter(recents) { recent -> openRecent(recent) }
    }

    private fun openRecent(recent: RecentFile) {
        val uri = Uri.parse(recent.uri)

        // Rebuild the sibling list from the parent folder, if we still remember it,
        // so page navigation (next/prev) keeps working from the Recents entry point too.
        var siblingUris = arrayListOf<Uri>()
        var siblingNames = arrayListOf<String>()
        var index = 0

        recent.parentTreeUri?.let { treeUriString ->
            try {
                val treeUri = Uri.parse(treeUriString)
                val dir = DocumentFile.fromTreeUri(this, treeUri)
                val files = dir?.listFiles()?.filter { it.isFile }.orEmpty()
                siblingUris = ArrayList(files.map { it.uri })
                siblingNames = ArrayList(files.map { it.name ?: "file" })
                index = files.indexOfFirst { it.uri == uri }.coerceAtLeast(0)
            } catch (_: Exception) {
                // Permission may have lapsed; fall back to opening the single file with no navigation.
            }
        }

        try {
            ViewerRouter.open(
                context = this,
                uri = uri,
                name = recent.name,
                mimeType = recent.mimeType,
                siblings = siblingUris,
                siblingNames = siblingNames,
                index = index
            )
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.file_open_error), Toast.LENGTH_SHORT).show()
        }
    }
}
