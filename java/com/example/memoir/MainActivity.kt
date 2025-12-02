package com.example.memoir

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedAdapter
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        val btnAdd = findViewById<FrameLayout>(R.id.navAdd)
        val btnGallery = findViewById<ImageView>(R.id.navGallery)
        val btnJournal = findViewById<ImageView>(R.id.navJournal)
        val btnTimeline = findViewById<ImageView>(R.id.navTimeline)
        val btnProfile = findViewById<ImageView>(R.id.btnHeaderLogout)
        val btnSearch = findViewById<ImageView>(R.id.btnHeaderSearch)

        recyclerView = findViewById(R.id.recyclerViewFeed)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 1. Setup Albums
        val recyclerAlbums = findViewById<RecyclerView>(R.id.recyclerAlbums)
        recyclerAlbums.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Ensure default album exists
        if (currentUserId != -1) dbHelper.createDefaultAlbum(currentUserId)

        loadAlbums(recyclerAlbums)

        // 2. Load Feed
        loadMixedFeed()

        // Listeners
        btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        btnSearch.setOnClickListener { startActivity(Intent(this, SearchActivity::class.java)) }
        btnAdd.setOnClickListener { startActivity(Intent(this, AddMemoryActivity::class.java)) }
        btnJournal.setOnClickListener { startActivity(Intent(this, JournalListActivity::class.java)) }
        btnTimeline.setOnClickListener { startActivity(Intent(this, TimelineActivity::class.java)) }
        btnGallery.setOnClickListener { startActivity(Intent(this, GalleryActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadMixedFeed()
    }

    private fun loadAlbums(recyclerView: RecyclerView) {
        if (currentUserId == -1) return
        val albums = dbHelper.getAllAlbums(currentUserId)
        val adapter = AlbumAdapter(albums,
            onAlbumClick = { album ->
                filterFeedByAlbum(album.id)
                Toast.makeText(this, "Album: ${album.name}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = {
                showCreateAlbumDialog()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showCreateAlbumDialog() {
        val input = EditText(this)
        input.hint = "Album Name"
        AlertDialog.Builder(this)
            .setTitle("New Album")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    dbHelper.addAlbum(currentUserId, name)
                    loadAlbums(findViewById(R.id.recyclerAlbums))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun filterFeedByAlbum(albumId: Int) {
        if (currentUserId != -1) {
            val filteredItems = dbHelper.getMemoriesByAlbum(currentUserId, albumId)
            adapter = FeedAdapter(filteredItems)
            recyclerView.adapter = adapter
        }
    }

    private fun loadMixedFeed() {
        if (currentUserId != -1) {
            val feedItems = dbHelper.getMixedFeed(currentUserId)
            adapter = FeedAdapter(feedItems)
            recyclerView.adapter = adapter
        }
    }
}