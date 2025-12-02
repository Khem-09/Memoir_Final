package com.example.memoir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JournalAdapter(private val journalList: List<Journal>) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvItemJournalDate)
        val content: TextView = itemView.findViewById(R.id.tvItemJournalContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_journal, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journal = journalList[position]
        holder.date.text = journal.date
        holder.content.text = journal.content
    }

    override fun getItemCount(): Int {
        return journalList.size
    }
}