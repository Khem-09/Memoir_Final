package com.example.memoir

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        dbHelper = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        // Toggle Password Visibility
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

        // Toggle Confirm Password Visibility
        etConfirmPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (etConfirmPassword.right - etConfirmPassword.compoundDrawables[2].bounds.width())) {
                    isConfirmPasswordVisible = !isConfirmPasswordVisible
                    if (isConfirmPasswordVisible) {
                        etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    } else {
                        etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (dbHelper.checkUserExists(user, email)) {
                Toast.makeText(this, "User or Email already exists!", Toast.LENGTH_SHORT).show()
            } else {
                val isInserted = dbHelper.addUser(user, email, pass)
                if (isInserted) {
                    Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }
}