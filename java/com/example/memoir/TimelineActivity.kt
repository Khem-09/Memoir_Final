package com.example.memoir

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimelineActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarView: CalendarView
    private lateinit var tvLabel: TextView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBackTimeline)
        calendarView = findViewById(R.id.calendarView)
        tvLabel = findViewById(R.id.tvTimelineLabel)
        recyclerView = findViewById(R.id.recyclerTimeline)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        // Get User ID
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        // 1. Load today's memories by default
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadMemoriesForDate(today)

        // 2. Handle Calendar Date Change
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Format: Month is 0-indexed (0 = Jan), so we add 1
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            tvLabel.text = "Memories on $selectedDate:"
            loadMemoriesForDate(selectedDate)
        }
    }

    private fun loadMemoriesForDate(date: String) {
        if (userId != -1) {
            val list = dbHelper.getMemoriesByDate(userId, date)

            // Reuse the existing MemoryAdapter!
            val adapter = MemoryAdapter(list)
            recyclerView.adapter = adapter
        }
    }
}