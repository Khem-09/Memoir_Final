package com.example.memoir

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        val btnAdd = findViewById<FrameLayout>(R.id.navAdd)
        val btnGallery = findViewById<ImageView>(R.id.navGallery)
        val btnJournal = findViewById<ImageView>(R.id.navJournal)
        val btnTimeline = findViewById<ImageView>(R.id.navTimeline)
        val btnProfile = findViewById<ImageView>(R.id.btnHeaderLogout)
        recyclerView = findViewById(R.id.recyclerViewFeed)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadMixedFeed()

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddMemoryActivity::class.java))
        }

        btnJournal.setOnClickListener {
            startActivity(Intent(this, JournalListActivity::class.java))
        }

        btnTimeline.setOnClickListener {
            startActivity(Intent(this, TimelineActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadMixedFeed()
    }

    private fun loadMixedFeed() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId != -1) {
            val feedItems = dbHelper.getMixedFeed(userId)
            adapter = FeedAdapter(feedItems)
            recyclerView.adapter = adapter
        }
    }
}