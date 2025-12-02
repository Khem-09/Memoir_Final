package com.example.memoir

data class Journal(
    override val id: Int,
    val content: String,
    override val date: String
) : FeedItem {
    override fun getType(): Int = FeedItem.TYPE_JOURNAL
}