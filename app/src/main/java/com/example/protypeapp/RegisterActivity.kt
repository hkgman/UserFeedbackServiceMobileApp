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
import com.example.protypeapp.models.Auth.RegisterRequest
import com.example.protypeapp.models.Auth.RegisterResponse
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var authController: AuthController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        authController = AuthController(this)
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

            when {
                email.isEmpty() -> {
                    Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show()
                }
                fullName.isEmpty() -> {
                    Toast.makeText(this, "Введите полное имя (Фамилия Имя Отчество)", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
                }
                confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Повторите пароль", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val nameParts = fullName.split(" ")
                    if (nameParts.size < 3) {
                        Toast.makeText(this, "Введите полное имя (Фамилия Имя Отчество)", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val surname = nameParts[0]
                    val name = nameParts[1]
                    val patronymic = nameParts[2]

                    val newUser = RegisterRequest(
                        surname = surname,
                        name = name,
                        email = email,
                        patronymic = patronymic,
                        password = password
                    )

                    authController.registerUser(newUser)
                }
            }
        }
        val loginLink: TextView = findViewById(R.id.tvLoginLink)
        loginLink.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}
