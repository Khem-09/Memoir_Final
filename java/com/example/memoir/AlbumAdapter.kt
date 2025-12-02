package com.example.memoir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlbumAdapter(
    private val albums: List<Album>,
    private val onAlbumClick: (Album) -> Unit,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_ADD_BUTTON = 0
        const val TYPE_ALBUM = 1
    }

    override fun getItemCount(): Int = albums.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD_BUTTON else TYPE_ALBUM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return if (viewType == TYPE_ADD_BUTTON) AddViewHolder(view) else AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ADD_BUTTON) {
            (holder as AddViewHolder).bind(onAddClick)
        } else {
            (holder as AlbumViewHolder).bind(albums[position - 1], onAlbumClick)
        }
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvAlbumName)
        val icon: ImageView = itemView.findViewById(R.id.imgAlbumIcon)
        fun bind(album: Album, onClick: (Album) -> Unit) {
            name.text = album.name
            icon.setImageResource(android.R.drawable.ic_menu_gallery)
            itemView.setOnClickListener { onClick(album) }
        }
    }

    class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvAlbumName)
        val icon: ImageView = itemView.findViewById(R.id.imgAlbumIcon)
        fun bind(onClick: () -> Unit) {
            name.text = "Create"
            icon.setImageResource(android.R.drawable.ic_input_add)
            itemView.setOnClickListener { onClick() }
        }
    }
}