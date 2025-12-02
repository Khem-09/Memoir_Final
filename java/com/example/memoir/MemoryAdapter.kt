package com.example.memoir

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2 // Import ViewPager2

class MemoryAdapter(private val memoryList: List<Memory>) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvItemTitle)
        val date: TextView = itemView.findViewById(R.id.tvItemDate)

        // CHANGED: We now look for the ViewPager2, NOT the ImageView
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPagerMemory)

        val category: TextView = itemView.findViewById(R.id.tvItemCategory)
        val caption: TextView = itemView.findViewById(R.id.tvItemCaption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = memoryList[position]

        holder.title.text = memory.title
        holder.date.text = memory.date
        holder.caption.text = memory.caption
        holder.category.text = memory.category.uppercase()

        // --- NEW SLIDER LOGIC ---
        // 1. Split the image path string into a list
        val imageList = memory.imageUri.split(",").filter { it.isNotEmpty() }

        // 2. Setup the adapter for the slider
        if (imageList.isNotEmpty()) {
            val sliderAdapter = ImageSliderAdapter(imageList)
            holder.viewPager.adapter = sliderAdapter
            holder.viewPager.visibility = View.VISIBLE
        } else {
            holder.viewPager.visibility = View.GONE
        }

        // Handle Click (Go to Detail Activity)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MemoryDetailActivity::class.java)
            intent.putExtra("MEMORY_ID", memory.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return memoryList.size
    }
}