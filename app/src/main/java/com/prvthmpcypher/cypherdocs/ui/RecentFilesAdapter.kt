package com.prvthmpcypher.cypherdocs.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.util.CypherFileType
import com.prvthmpcypher.cypherdocs.util.FileTypeUtils
import com.prvthmpcypher.cypherdocs.util.RecentFile
import java.text.DateFormat
import java.util.Date

class RecentFilesAdapter(
    private val items: List<RecentFile>,
    private val onClick: (RecentFile) -> Unit
) : RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.image_file_icon)
        val name: TextView = view.findViewById(R.id.text_file_name)
        val meta: TextView = view.findViewById(R.id.text_file_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name

        val type = FileTypeUtils.classify(item.name, item.mimeType)
        holder.icon.setImageResource(
            when (type) {
                CypherFileType.IMAGE -> R.drawable.ic_image
                else -> R.drawable.ic_file_generic
            }
        )

        val when_ = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.timestamp))
        holder.meta.text = when_

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
