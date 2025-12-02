package com.example.memoir

data class Memory(
    override val id: Int,
    val title: String,
    val caption: String,
    override val date: String,
    val imageUri: String,
    val albumId: Int,      // NEW: Links to Album ID
    val albumName: String  // NEW: Store name for display convenience
) : FeedItem {
    override fun getType(): Int = FeedItem.TYPE_MEMORY
}