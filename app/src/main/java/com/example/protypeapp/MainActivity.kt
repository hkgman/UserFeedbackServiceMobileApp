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
import com.example.protypeapp.models.Auth.UserRequest

class MainActivity : AppCompatActivity(),AuthListener {
    private lateinit var authController: AuthController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authController = AuthController(this,this)
        if (authController.isUserLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        val loginButton: Button = findViewById(R.id.btnLogin)
        loginButton.setOnClickListener {
            val login = findViewById<EditText>(R.id.etLogin).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()
            authController.loginUser(login,password)
        }
        val registerLink: TextView = findViewById(R.id.tvRegisterLink)
        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
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
