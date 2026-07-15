package com.prvthmpcypher.cypherdocs.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.util.RecentFilesStore
import com.prvthmpcypher.cypherdocs.util.RecentFile
import com.prvthmpcypher.cypherdocs.viewer.ViewerRouter

class FolderBrowserActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TREE_URI = "extra_tree_uri"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var treeUri: Uri
    private var entries: List<FolderEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_browser)

        treeUri = getUriExtraCompat(EXTRA_TREE_URI) ?: run { finish(); return }
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Folder"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)

        val recycler = findViewById<RecyclerView>(R.id.recycler_folder_files)
        recycler.layoutManager = LinearLayoutManager(this)

        val emptyText = findViewById<TextView>(R.id.text_empty_folder)

        entries = loadEntries(treeUri)
        emptyText.visibility = if (entries.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        recycler.adapter = FolderEntriesAdapter(entries) { entry, _ ->
            if (entry.isDirectory) {
                val nested = android.content.Intent(this, FolderBrowserActivity::class.java).apply {
                    putExtra(EXTRA_TREE_URI, entry.uri)
                    putExtra(EXTRA_TITLE, entry.name)
                }
                startActivity(nested)
            } else {
                openFile(entry)
            }
        }
    }

    private fun openFile(entry: FolderEntry) {
        val fileEntries = entries.filter { !it.isDirectory }
        val siblingUris = ArrayList(fileEntries.map { it.uri })
        val siblingNames = ArrayList(fileEntries.map { it.name })
        val index = fileEntries.indexOfFirst { it.uri == entry.uri }.coerceAtLeast(0)

        RecentFilesStore.add(
            this,
            RecentFile(
                uri = entry.uri.toString(),
                name = entry.name,
                mimeType = entry.mimeType,
                parentTreeUri = treeUri.toString(),
                timestamp = System.currentTimeMillis()
            )
        )

        ViewerRouter.open(
            context = this,
            uri = entry.uri,
            name = entry.name,
            mimeType = entry.mimeType,
            siblings = siblingUris,
            siblingNames = siblingNames,
            index = index
        )
    }

    private fun loadEntries(uri: Uri): List<FolderEntry> {
        val dir = DocumentFile.fromTreeUri(this, uri) ?: return emptyList()
        val children = dir.listFiles().toList()
        val folders = children.filter { it.isDirectory }.sortedBy { it.name?.lowercase() }
        val files = children.filter { it.isFile }.sortedBy { it.name?.lowercase() }

        return (folders + files).map {
            FolderEntry(
                uri = it.uri,
                name = it.name ?: "unnamed",
                mimeType = it.type,
                isDirectory = it.isDirectory,
                sizeBytes = it.length()
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun getUriExtraCompat(key: String): Uri? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(key, Uri::class.java)
        } else {
            intent.getParcelableExtra(key)
        }
    }
}
