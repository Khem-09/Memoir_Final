package com.example.memoir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tvLabel: TextView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBackSearch)
        val btnSearch = findViewById<ImageView>(R.id.btnDoSearch)
        etSearch = findViewById(R.id.etSearchQuery)
        tvLabel = findViewById(R.id.tvResultsLabel)
        recyclerView = findViewById(R.id.recyclerSearch)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        btnBack.setOnClickListener { finish() }

        btnSearch.setOnClickListener {
            performSearch(etSearch.text.toString())
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun performSearch(query: String) {
        if (userId != -1 && query.isNotEmpty()) {
            val results = dbHelper.searchFeed(userId, query)

            if (results.isNotEmpty()) {
                tvLabel.text = "Found ${results.size} matches"
                tvLabel.visibility = View.VISIBLE
                val adapter = FeedAdapter(results)
                recyclerView.adapter = adapter
            } else {
                tvLabel.text = "No matches found"
                tvLabel.visibility = View.VISIBLE
                recyclerView.adapter = null
            }
        } else {
            recyclerView.adapter = null
            tvLabel.visibility = View.GONE
        }
    }
}