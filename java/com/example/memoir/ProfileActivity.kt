package com.example.memoir

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var imgProfile: ImageView
    private lateinit var etName: EditText
    private var selectedImageUri: Uri? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)

        // Initialize Views
        imgProfile = findViewById(R.id.imgProfile)
        etName = findViewById(R.id.etEditName)
        val btnSave = findViewById<Button>(R.id.btnSaveChanges)
        val btnLogout = findViewById<Button>(R.id.btnLogoutFromProfile)

        // --- NEW: Back Button Logic ---
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Closes this activity and goes back to the previous one (Dashboard)
        }
        // ------------------------------

        // 1. Get User ID
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. Load Data
        loadUserData()

        // 3. Image Picker
        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        // 4. Save
        btnSave.setOnClickListener {
            val newName = etName.text.toString()
            val uriString = selectedImageUri?.toString()

            val success = dbHelper.updateProfile(userId, newName, uriString)
            if (success) {
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Logout
        btnLogout.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val cursor = dbHelper.getUserDetails(userId)
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex("username")
            val picIndex = cursor.getColumnIndex("profile_pic")

            if (nameIndex != -1) {
                val name = cursor.getString(nameIndex)
                etName.setText(name)
            }

            if (picIndex != -1) {
                val imageUriStr = cursor.getString(picIndex)
                if (imageUriStr != null) {
                    imgProfile.setImageURI(Uri.parse(imageUriStr))
                    selectedImageUri = Uri.parse(imageUriStr)
                }
            }
        }
        cursor.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imgProfile.setImageURI(selectedImageUri)
        }
    }
}