package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.protypeapp.controller.AuthController
import com.example.protypeapp.controller.Listeners.AuthListener
import com.example.protypeapp.models.Auth.RegisterRequest

class RegisterActivity : AppCompatActivity(),AuthListener {
    private lateinit var authController: AuthController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        authController = AuthController(this,this)
        if (authController.isUserLoggedIn()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        val registerButton: Button = findViewById(R.id.btnRegister)
        registerButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val fullName = findViewById<EditText>(R.id.etFullName).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString().trim()

            authController.registerUser(email, fullName, password, confirmPassword)
        }
        val loginLink: TextView = findViewById(R.id.tvLoginLink)
        loginLink.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMessage(message: String) {
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
