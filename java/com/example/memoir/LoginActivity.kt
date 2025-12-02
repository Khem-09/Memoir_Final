package com.example.memoir

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val etInput = findViewById<EditText>(R.id.etLoginUsername)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignupLink = findViewById<TextView>(R.id.tvSignupLink)

        // Password Eye Toggle
        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (etPassword.right - etPassword.compoundDrawables[2].bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    } else {
                        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        btnLogin.setOnClickListener {
            val input = etInput.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (input.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Get User ID from DB
                val userId = dbHelper.checkUser(input, pass)

                if (userId != -1) {
                    // SAVE SESSION
                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putInt("USER_ID", userId)
                    editor.apply()

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvSignupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}