package com.example.memoir

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class EditMemoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerImages: LinearLayout
    private lateinit var etDate: EditText
    private lateinit var spinnerAlbums: Spinner

    // We store paths (Strings) because some are old files, some are new URIs
    private val currentImagePaths = ArrayList<String>()
    private var albumList = ArrayList<Album>()

    private var memoryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memory)

        dbHelper = DatabaseHelper(this)

        // Bind Views (Note: We use containerImages, NOT imgPreview)
        val btnAddPhotos = findViewById<LinearLayout>(R.id.btnAddPhotosEdit)
        containerImages = findViewById(R.id.containerEditImages)
        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etCaption = findViewById<EditText>(R.id.etEditCaption)
        etDate = findViewById(R.id.etEditDate)

        // FIX: We use spinnerAlbums, NOT etCategory
        spinnerAlbums = findViewById(R.id.spinnerAlbums)

        val btnUpdate = findViewById<Button>(R.id.btnUpdateMemory)
        val btnBack = findViewById<ImageView>(R.id.btnBackEdit)

        btnBack.setOnClickListener { finish() }

        // 1. Load Albums into Spinner
        loadAlbumSpinner()

        // 2. Load Memory Data
        memoryId = intent.getIntExtra("MEMORY_ID", -1)
        if (memoryId != -1) {
            val memory = dbHelper.getMemory(memoryId)
            if (memory != null) {
                etTitle.setText(memory.title)
                etCaption.setText(memory.caption)
                etDate.setText(memory.date)

                // FIX: Select the correct album in the spinner using albumId
                val albumIndex = albumList.indexOfFirst { it.id == memory.albumId }
                if (albumIndex != -1) {
                    spinnerAlbums.setSelection(albumIndex)
                }

                // FIX: Handle Multiple Images
                val savedPaths = memory.imageUri.split(",").filter { it.isNotEmpty() }
                currentImagePaths.addAll(savedPaths)
                displayImages()
            }
        }

        etDate.setOnClickListener { showDatePicker() }

        // Add MORE photos logic
        btnAddPhotos.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, 300)
        }

        btnUpdate.setOnClickListener {
            val title = etTitle.text.toString()
            val caption = etCaption.text.toString()
            val date = etDate.text.toString()

            // Get selected Album ID
            val selectedAlbum = albumList[spinnerAlbums.selectedItemPosition]
            val albumId = selectedAlbum.id

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
            } else {
                // Join the list back into a string
                val finalImageUriString = currentImagePaths.joinToString(",")

                // FIX: Pass albumId (Int) to the database
                val success = dbHelper.updateMemory(memoryId, title, caption, date, finalImageUriString, albumId)

                if (success) {
                    Toast.makeText(this, "Memory Updated!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadAlbumSpinner() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId != -1) {
            albumList = dbHelper.getAllAlbums(userId) as ArrayList<Album>
            val albumNames = albumList.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, albumNames)
            spinnerAlbums.adapter = adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == Activity.RESULT_OK && data != null) {

            // Process NEW images
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    val path = saveImageToInternalStorage(uri)
                    if(path.isNotEmpty()) currentImagePaths.add(path)
                }
            } else if (data.data != null) {
                val path = saveImageToInternalStorage(data.data!!)
                if(path.isNotEmpty()) currentImagePaths.add(path)
            }
            displayImages()
        }
    }

    private fun displayImages() {
        containerImages.removeAllViews()
        for (path in currentImagePaths) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT)
            params.setMargins(0, 0, 16, 0)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(Uri.parse(path))
            imageView.background = getDrawable(R.drawable.bg_card_white)
            imageView.clipToOutline = true

            containerImages.addView(imageView)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        return try {
            val fileName = "memoir_${System.currentTimeMillis()}_${(0..1000).random()}.jpg"
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file).toString()
        } catch (e: Exception) { "" }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
            etDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
        }, year, month, day)
        datePickerDialog.show()
    }
}