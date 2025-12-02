package com.example.memoir

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GalleryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBackGallery)
        recyclerView = findViewById(R.id.recyclerGallery)

        // Set up 3-Column Grid
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        btnBack.setOnClickListener { finish() }

        loadGallery()
    }

    private fun loadGallery() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId != -1) {
            // Re-using the getAllMemories function we already wrote
            val memoryList = dbHelper.getAllMemories(userId)
            val adapter = GalleryAdapter(memoryList)
            recyclerView.adapter = adapter
        }
    }
}