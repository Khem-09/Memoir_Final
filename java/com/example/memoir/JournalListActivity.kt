package com.example.memoir

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class JournalListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_list)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerJournal)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnBack = findViewById<ImageView>(R.id.btnBackJournalList)
        val btnAdd = findViewById<ImageView>(R.id.btnAddJournalEntry)

        btnBack.setOnClickListener { finish() }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, WriteJournalActivity::class.java))
        }

        loadJournals()
    }

    override fun onResume() {
        super.onResume()
        loadJournals() // Refresh list when returning from writing
    }

    private fun loadJournals() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId != -1) {
            val list = dbHelper.getAllJournals(userId)
            val adapter = JournalAdapter(list)
            recyclerView.adapter = adapter
        }
    }
}