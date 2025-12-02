package com.example.memoir

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class EditMemoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var imgPreview: ImageView
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var etDate: EditText
    private var selectedImageUri: Uri? = null
    private var memoryId: Int = -1
    private var currentImageUri: String = "" // To keep the old image if user doesn't change it

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memory)

        dbHelper = DatabaseHelper(this)

        // Bind Views
        imgPreview = findViewById(R.id.imgEditPreview)
        layoutPlaceholder = findViewById(R.id.layoutEditPlaceholder)
        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etCaption = findViewById<EditText>(R.id.etEditCaption)
        etDate = findViewById(R.id.etEditDate)
        val etCategory = findViewById<EditText>(R.id.etEditCategory)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateMemory)
        val btnBack = findViewById<ImageView>(R.id.btnBackEdit)

        btnBack.setOnClickListener { finish() }

        // 1. Get Memory ID and Pre-fill Data
        memoryId = intent.getIntExtra("MEMORY_ID", -1)
        if (memoryId != -1) {
            val memory = dbHelper.getMemory(memoryId)
            if (memory != null) {
                etTitle.setText(memory.title)
                etCaption.setText(memory.caption)
                etDate.setText(memory.date)
                etCategory.setText(memory.category)

                currentImageUri = memory.imageUri
                if (currentImageUri.isNotEmpty()) {
                    imgPreview.setImageURI(Uri.parse(currentImageUri))
                    layoutPlaceholder.visibility = android.view.View.GONE
                    selectedImageUri = Uri.parse(currentImageUri)
                }
            }
        }

        // 2. Date Picker
        etDate.setOnClickListener { showDatePicker() }

        // 3. Image Picker
        imgPreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 300)
        }

        // 4. Update Logic
        btnUpdate.setOnClickListener {
            val title = etTitle.text.toString()
            val caption = etCaption.text.toString()
            val date = etDate.text.toString()
            val category = etCategory.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
            } else {
                // If user picked a NEW image, save it. Otherwise keep OLD image.
                var finalImageUri = currentImageUri

                // If the selected URI is different from the one we loaded, user changed it
                if (selectedImageUri != null && selectedImageUri.toString() != currentImageUri) {
                    finalImageUri = saveImageToInternalStorage(selectedImageUri!!)
                }

                val success = dbHelper.updateMemory(memoryId, title, caption, date, finalImageUri, category)
                if (success) {
                    Toast.makeText(this, "Memory Updated!", Toast.LENGTH_SHORT).show()

                    // Go back to Main Dashboard (Clear stack so data refreshes)
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

    // Reuse helper function
    private fun saveImageToInternalStorage(uri: Uri): String {
        return try {
            val fileName = "memoir_${System.currentTimeMillis()}.jpg"
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

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etDate.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imgPreview.setImageURI(selectedImageUri)
            layoutPlaceholder.visibility = android.view.View.GONE
        }
    }
}