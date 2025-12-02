package com.example.memoir

data class Memory(
    override val id: Int,
    val title: String,
    val caption: String,
    override val date: String,
    val imageUri: String, // Stores "path1,path2,path3"
    val category: String
) : FeedItem {
    override fun getType(): Int = FeedItem.TYPE_MEMORY
}