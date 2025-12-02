package com.example.memoir

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MemoryAdapter(private val memoryList: List<Memory>) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvItemTitle)
        val date: TextView = itemView.findViewById(R.id.tvItemDate)
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPagerMemory)
        val tabLayout: TabLayout = itemView.findViewById(R.id.tabLayoutDots)

        // FIXED: Changed from tvItemCategory to tvItemAlbum
        val albumBadge: TextView = itemView.findViewById(R.id.tvItemAlbum)

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

        // FIXED: Use albumName instead of category
        holder.albumBadge.text = memory.albumName.uppercase()

        // --- CAROUSEL LOGIC ---
        val imageList = memory.imageUri.split(",").filter { it.isNotEmpty() }

        if (imageList.isNotEmpty()) {
            val sliderAdapter = ImageSliderAdapter(imageList)
            holder.viewPager.adapter = sliderAdapter
            holder.viewPager.visibility = View.VISIBLE

            // Show dots ONLY if there is more than 1 image
            if (imageList.size > 1) {
                holder.tabLayout.visibility = View.VISIBLE
                TabLayoutMediator(holder.tabLayout, holder.viewPager) { _, _ -> }.attach()
            } else {
                holder.tabLayout.visibility = View.GONE
            }
        } else {
            holder.viewPager.visibility = View.GONE
            holder.tabLayout.visibility = View.GONE
        }

        // Click Logic
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