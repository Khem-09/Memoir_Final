package com.example.memoir

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GalleryAdapter(private val memoryList: List<Memory>) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgGalleryGrid)
        val iconMulti: ImageView = itemView.findViewById(R.id.iconMultiPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_grid, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val memory = memoryList[position]

        // 1. Get the first image to show as thumbnail
        val imagePaths = memory.imageUri.split(",").filter { it.isNotEmpty() }
        val firstImage = imagePaths.firstOrNull()

        if (firstImage != null) {
            try {
                holder.image.setImageURI(Uri.parse(firstImage))
            } catch (e: Exception) {
                holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // 2. Show icon if there are multiple photos
        if (imagePaths.size > 1) {
            holder.iconMulti.visibility = View.VISIBLE
        } else {
            holder.iconMulti.visibility = View.GONE
        }

        // 3. Click to open Detail View
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MemoryDetailActivity::class.java)
            intent.putExtra("MEMORY_ID", memory.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = memoryList.size
}