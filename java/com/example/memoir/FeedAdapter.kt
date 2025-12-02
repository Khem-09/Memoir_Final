package com.example.memoir

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FeedAdapter(private val itemList: List<FeedItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvItemTitle)
        val date: TextView = itemView.findViewById(R.id.tvItemDate)
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPagerMemory)
        val tabLayout: TabLayout = itemView.findViewById(R.id.tabLayoutDots)

        // RENAMED VARIABLE
        val albumBadge: TextView = itemView.findViewById(R.id.tvItemAlbum)

        val caption: TextView = itemView.findViewById(R.id.tvItemCaption)
    }

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvItemJournalDate)
        val content: TextView = itemView.findViewById(R.id.tvItemJournalContent)
    }

    override fun getItemViewType(position: Int): Int = itemList[position].getType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FeedItem.TYPE_MEMORY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
            MemoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_journal, parent, false)
            JournalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]

        if (getItemViewType(position) == FeedItem.TYPE_MEMORY) {
            val memory = item as Memory
            val mHolder = holder as MemoryViewHolder

            mHolder.title.text = memory.title
            mHolder.date.text = memory.date
            mHolder.caption.text = memory.caption

            // SHOW ALBUM NAME
            mHolder.albumBadge.text = memory.albumName.uppercase()

            // Slider Logic
            val imageList = memory.imageUri.split(",").filter { it.isNotEmpty() }
            if (imageList.isNotEmpty()) {
                val sliderAdapter = ImageSliderAdapter(imageList)
                mHolder.viewPager.adapter = sliderAdapter
                mHolder.viewPager.visibility = View.VISIBLE

                if (imageList.size > 1) {
                    mHolder.tabLayout.visibility = View.VISIBLE
                    TabLayoutMediator(mHolder.tabLayout, mHolder.viewPager) { _, _ -> }.attach()
                } else {
                    mHolder.tabLayout.visibility = View.GONE
                }
            } else {
                mHolder.viewPager.visibility = View.GONE
                mHolder.tabLayout.visibility = View.GONE
            }

            mHolder.itemView.setOnClickListener {
                val context = mHolder.itemView.context
                val intent = Intent(context, MemoryDetailActivity::class.java)
                intent.putExtra("MEMORY_ID", memory.id)
                context.startActivity(intent)
            }

        } else {
            val journal = item as Journal
            val jHolder = holder as JournalViewHolder
            jHolder.date.text = journal.date
            jHolder.content.text = journal.content
        }
    }

    override fun getItemCount(): Int = itemList.size
}