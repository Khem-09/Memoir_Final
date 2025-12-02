package com.example.memoir

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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

    // Stores the list of photos you picked
    private val selectedImageUris = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memory)

        dbHelper = DatabaseHelper(this)

        val btnAddPhotos = findViewById<LinearLayout>(R.id.btnAddPhotos)
        containerSelectedImages = findViewById(R.id.containerSelectedImages)
        val etTitle = findViewById<EditText>(R.id.etMemoryTitle)
        val etCaption = findViewById<EditText>(R.id.etMemoryCaption)
        etDate = findViewById(R.id.etMemoryDate)
        val etCategory = findViewById<EditText>(R.id.etMemoryCategory)
        val btnSave = findViewById<Button>(R.id.btnSaveMemory)
        val btnBack = findViewById<ImageView>(R.id.btnBackAdd)

        btnBack.setOnClickListener { finish() }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(currentDate)
        etDate.setOnClickListener { showDatePicker() }

        // OPEN GALLERY (Supports Multi-Select)
        btnAddPhotos.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, 200)
        }

        btnSave.setOnClickListener {
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val userId = sharedPref.getInt("USER_ID", -1)

            if (userId == -1) {
                Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            val title = etTitle.text.toString()
            val caption = etCaption.text.toString()
            val date = etDate.text.toString()
            val category = etCategory.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
            } else {
                // Save all images
                val savedPaths = ArrayList<String>()
                for (uri in selectedImageUris) {
                    val internalPath = saveImageToInternalStorage(uri)
                    if (internalPath.isNotEmpty()) {
                        savedPaths.add(internalPath)
                    }
                }

                // Join them into one string: "path1,path2,path3"
                val finalImageUriString = savedPaths.joinToString(",")

                val success = dbHelper.addMemory(userId, title, caption, date, finalImageUriString, category)
                if (success) {
                    Toast.makeText(this, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {

            // NOTE: We do NOT clear the list here, so you can add more photos if you click the button again

            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    selectedImageUris.add(uri)
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