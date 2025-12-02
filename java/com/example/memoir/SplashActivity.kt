package com.example.memoir

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wait for 2.5 seconds (2500ms) then decide where to go
        Handler(Looper.getMainLooper()).postDelayed({
            checkSession()
        }, 2500)
    }

    private fun checkSession() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (userId != -1) {
            // User is already logged in -> Go to Dashboard
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // No user found -> Go to Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Close Splash Activity so user can't go back to it
        finish()
    }
}