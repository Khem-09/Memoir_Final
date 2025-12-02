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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddMemoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerSelectedImages: LinearLayout
    private lateinit var etDate: EditText
    private lateinit var spinnerAlbums: Spinner

    private val selectedImageUris = ArrayList<Uri>()
    private var albumList = ArrayList<Album>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memory)

        dbHelper = DatabaseHelper(this)

        val btnAddPhotos = findViewById<LinearLayout>(R.id.btnAddPhotos)
        containerSelectedImages = findViewById(R.id.containerSelectedImages)
        val etTitle = findViewById<EditText>(R.id.etMemoryTitle)
        val etCaption = findViewById<EditText>(R.id.etMemoryCaption)
        etDate = findViewById(R.id.etMemoryDate)
        spinnerAlbums = findViewById(R.id.spinnerAlbums)
        val btnSave = findViewById<Button>(R.id.btnSaveMemory)
        val btnBack = findViewById<ImageView>(R.id.btnBackAdd)

        btnBack.setOnClickListener { finish() }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(currentDate)
        etDate.setOnClickListener { showDatePicker() }

        // LOAD SPINNER
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId != -1) {
            albumList = dbHelper.getAllAlbums(userId) as ArrayList<Album>
            val albumNames = albumList.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, albumNames)
            spinnerAlbums.adapter = adapter
        }

        btnAddPhotos.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, 200)
        }

        btnSave.setOnClickListener {
            if (userId == -1) { finish(); return@setOnClickListener }

            val title = etTitle.text.toString()
            val caption = etCaption.text.toString()
            val date = etDate.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get Album ID
            val selectedAlbum = albumList[spinnerAlbums.selectedItemPosition]
            val albumId = selectedAlbum.id

            // Save Images
            val savedPaths = ArrayList<String>()
            for (uri in selectedImageUris) {
                val internalPath = saveImageToInternalStorage(uri)
                if (internalPath.isNotEmpty()) savedPaths.add(internalPath)
            }
            val finalImageUriString = savedPaths.joinToString(",")

            val success = dbHelper.addMemory(userId, title, caption, date, finalImageUriString, albumId)
            if (success) {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    // (Keep onActivityResult, displaySelectedImages, saveImageToInternalStorage, showDatePicker from previous step)
    // ... Copy them here ...
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    selectedImageUris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data.data != null) {
                selectedImageUris.add(data.data!!)
            }
            displaySelectedImages()
        }
    }

    private fun displaySelectedImages() {
        containerSelectedImages.removeAllViews()
        for (uri in selectedImageUris) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT)
            params.setMargins(0, 0, 16, 0)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(uri)
            imageView.background = getDrawable(R.drawable.bg_card_white)
            imageView.clipToOutline = true
            containerSelectedImages.addView(imageView)
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