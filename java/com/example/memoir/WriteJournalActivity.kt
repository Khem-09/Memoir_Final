package com.example.memoir

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WriteJournalActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_journal)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBackJournal)
        val btnSave = findViewById<ImageView>(R.id.btnSaveJournal)
        val tvDate = findViewById<TextView>(R.id.tvJournalDate)
        val etContent = findViewById<EditText>(R.id.etJournalContent)

        // Set Date Header
        val currentDate = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date())
        val dbDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        tvDate.text = currentDate

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val content = etContent.text.toString().trim()
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val userId = sharedPref.getInt("USER_ID", -1)

            if (content.isEmpty()) {
                Toast.makeText(this, "Journal cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (userId != -1) {
                val success = dbHelper.addJournal(userId, content, dbDate)
                if (success) {
                    Toast.makeText(this, "Journal Saved", Toast.LENGTH_SHORT).show()
                    finish() // Close and go back to list
                } else {
                    Toast.makeText(this, "Error Saving", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}