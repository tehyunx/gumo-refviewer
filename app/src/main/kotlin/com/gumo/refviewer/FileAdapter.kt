package com.gumo.refviewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(private val onClick: (File) -> Unit) : RecyclerView.Adapter<FileAdapter.VH>() {
    private val items = mutableListOf<FileItem>()

    fun setFiles(list: List<FileItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], onClick)
    override fun getItemCount() = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvTags: TextView = view.findViewById(R.id.tv_tags)
        private val tvExt: TextView = view.findViewById(R.id.tv_ext)

        fun bind(item: FileItem, onClick: (File) -> Unit) {
            tvTitle.text = item.displayTitle
            tvExt.text = item.file.extension.uppercase()
            if (item.tags.isEmpty()) {
                tvTags.visibility = View.GONE
            } else {
                tvTags.visibility = View.VISIBLE
                tvTags.text = item.tags.joinToString(" · ")
            }
            itemView.setOnClickListener { onClick(item.file) }
        }
    }
}
