package com.example.memoir

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageSliderAdapter(private val images: List<String>) : RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider_image, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val imagePath = images[position]
        try {
            holder.imageView.setImageURI(Uri.parse(imagePath))
        } catch (e: Exception) {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount(): Int = images.size
}