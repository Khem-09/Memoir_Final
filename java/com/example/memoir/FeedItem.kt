package com.example.memoir

interface FeedItem {
    val id: Int
    val date: String
    fun getType(): Int

    companion object {
        const val TYPE_MEMORY = 0
        const val TYPE_JOURNAL = 1
    }
}