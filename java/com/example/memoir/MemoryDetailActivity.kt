package com.example.memoir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class MemoryDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var memoryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_detail)

        dbHelper = DatabaseHelper(this)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerDetail)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvCaption = findViewById<TextView>(R.id.tvDetailCaption)
        val btnBack = findViewById<ImageView>(R.id.btnDetailBack)
        val btnDelete = findViewById<Button>(R.id.btnDeleteMemory)
        val btnEdit = findViewById<Button>(R.id.btnEditMemory)

        // RENAMED BINDING
        val tvAlbum = findViewById<TextView>(R.id.tvDetailAlbum)

        memoryId = intent.getIntExtra("MEMORY_ID", -1)

        if (memoryId != -1) {
            val memory = dbHelper.getMemory(memoryId)
            if (memory != null) {
                tvTitle.text = memory.title
                tvDate.text = memory.date
                tvCaption.text = memory.caption

                // USE ALBUM NAME
                tvAlbum.text = memory.albumName.uppercase()

                val imageList = memory.imageUri.split(",").filter { it.isNotEmpty() }
                if (imageList.isNotEmpty()) {
                    val sliderAdapter = ImageSliderAdapter(imageList)
                    viewPager.adapter = sliderAdapter
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditMemoryActivity::class.java)
            intent.putExtra("MEMORY_ID", memoryId)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Memory?")
            builder.setMessage("Are you sure you want to delete this memory?")
            builder.setPositiveButton("Yes") { _, _ ->
                if (dbHelper.deleteMemory(memoryId)) {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            builder.setNegativeButton("No") { d, _ -> d.dismiss() }
            builder.show()
        }
    }
}