package com.example.protypeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.AuthService
import com.example.protypeapp.controller.AuthController
import com.example.protypeapp.models.Auth.LoginResponse
import com.example.protypeapp.models.Auth.UserRequest
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var authController: AuthController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authController = AuthController(this)
        if (authController.isUserLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        val loginButton: Button = findViewById(R.id.btnLogin)
        loginButton.setOnClickListener {
            val login = findViewById<EditText>(R.id.etLogin).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()
            when {
                login.isEmpty() -> {
                    Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
                }
                else->{
                    val user = UserRequest(
                        email = login,
                        password = password
                    )
                    authController.loginUser(user)

                }
            }
        }
        val registerLink: TextView = findViewById(R.id.tvRegisterLink)
        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

}
