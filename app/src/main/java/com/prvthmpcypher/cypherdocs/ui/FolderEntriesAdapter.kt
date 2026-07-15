package com.prvthmpcypher.cypherdocs.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.util.CypherFileType
import com.prvthmpcypher.cypherdocs.util.FileTypeUtils

data class FolderEntry(
    val uri: Uri,
    val name: String,
    val mimeType: String?,
    val isDirectory: Boolean,
    val sizeBytes: Long
)

class FolderEntriesAdapter(
    private val entries: List<FolderEntry>,
    private val onClick: (FolderEntry, Int) -> Unit
) : RecyclerView.Adapter<FolderEntriesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.image_row_icon)
        val name: TextView = view.findViewById(R.id.text_row_name)
        val meta: TextView = view.findViewById(R.id.text_row_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.name.text = entry.name

        if (entry.isDirectory) {
            holder.icon.setImageResource(R.drawable.ic_folder)
            holder.meta.text = "Folder"
        } else {
            val type = FileTypeUtils.classify(entry.name, entry.mimeType)
            holder.icon.setImageResource(
                when (type) {
                    CypherFileType.IMAGE -> R.drawable.ic_image
                    CypherFileType.PDF_EXCLUDED -> R.drawable.ic_file_generic
                    else -> R.drawable.ic_file_generic
                }
            )
            val ext = FileTypeUtils.extensionOf(entry.name).uppercase().ifBlank { "FILE" }
            val label = if (type == CypherFileType.PDF_EXCLUDED) "$ext · not supported here" else ext
            holder.meta.text = label
        }

        holder.itemView.setOnClickListener { onClick(entry, position) }
    }

    override fun getItemCount() = entries.size
}
