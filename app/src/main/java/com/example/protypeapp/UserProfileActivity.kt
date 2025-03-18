package com.example.protypeapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.protypeapp.API.ApiClient
import com.example.protypeapp.API.ApiService
import com.example.protypeapp.models.User.UpdateUserRequest
import com.example.protypeapp.models.User.UpdateUserResponse
import com.example.protypeapp.models.User.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


class UserProfileActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var textFio: EditText
    private lateinit var textEmail: EditText
    private lateinit var editButton: Button
    private lateinit var exitButton: Button
    private lateinit var userPreferences: UserPreferences

    private var initialFio: String = ""
    private var initialEmail: String = ""
    private var initialImage: Bitmap? = null
    private var isImageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        image = findViewById(R.id.ivProfileImage)
        textFio = findViewById(R.id.etFullName)
        textEmail = findViewById(R.id.etEmail)
        editButton = findViewById(R.id.btnEdit)
        exitButton = findViewById(R.id.btnExit)

        userPreferences = UserPreferences(this)

        fetchUserInfo()

        setupChangeListeners()

        editButton.setOnClickListener {
            updateUser()
        }

        exitButton.setOnClickListener {
            userPreferences.logout()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Companion.REQUEST_CODE_PICK_IMAGE)
        }
    }

    private fun setupChangeListeners() {
        textFio.addTextChangedListener {
            checkForChanges()
        }

        textEmail.addTextChangedListener {
            checkForChanges()
        }
    }

    private fun checkForChanges() {
        val isFioChanged = textFio.text.toString() != initialFio
        val isEmailChanged = textEmail.text.toString() != initialEmail

        editButton.isEnabled = isFioChanged || isEmailChanged || isImageChanged
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                try {
                    val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, selectedImageUri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    }

                    image.setImageBitmap(bitmap)
                    isImageChanged = true
                    checkForChanges()
                } catch (e: Exception) {
                    Toast.makeText(this, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
    }

    private fun fetchUserInfo() {
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val call = apiService.getUser()

        call.enqueue(object : Callback<UserInfo> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        initialFio = "${user.surname} ${user.name} ${user.patronymic}"
                        initialEmail = user.email

                        textFio.setText(initialFio)
                        textEmail.setText(initialEmail)

                        if (user.image != null && user.image != "none") {
                            val imageBytes = decodeBase64(user.image)
                            if (imageBytes != null) {
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                image.setImageBitmap(bitmap)
                                initialImage = bitmap // Сохраняем начальное изображение
                            } else {
                                image.setImageResource(R.drawable.person)
                            }
                        } else {
                            image.setImageResource(R.drawable.person)
                        }
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Полученные данные пусты", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError()
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        val jsonObject = JSONObject(errorResponse!!)
                        val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                        Toast.makeText(this@UserProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUser() {
        val fioText = textFio.text.toString().trim()
        val fioParts = fioText.split(" ")

        if (fioParts.size != 3) {
            Toast.makeText(this, "Введите ФИО в формате: Фамилия Имя Отчество", Toast.LENGTH_SHORT).show()
            return
        }

        val surname = fioParts[0]
        val name = fioParts[1]
        val patronymic = fioParts[2]

        showPasswordDialog { password ->
            sendUpdateRequest(surname,name,patronymic,password)
        }

    }
    private fun showPasswordDialog(onPasswordEntered: (String) -> Unit) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Введите пароль")
            .setView(input)
            .setPositiveButton("ОК") { dialog, which ->
                val password = input.text.toString()
                onPasswordEntered(password)
            }
            .setNegativeButton("Отмена") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }
    private fun sendUpdateRequest(surname: String, name: String, patronymic: String, password: String){
        val apiService = ApiClient.getClient(this).create(ApiService::class.java)
        val drawable = image.drawable
        val imageBase64 = if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } else {
            null
        }

        val updateUserRequest = UpdateUserRequest(
            name = name,
            surname = surname,
            patronymic = patronymic,
            email = textEmail.text.toString(),
            image = imageBase64,
            password=password
        )

        val call = apiService.updateUser(updateUserRequest)
        call.enqueue(object : Callback<UpdateUserResponse> {
            override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse?.user_id != null) {
                        Toast.makeText(this@UserProfileActivity, updateResponse.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Не удалось обновить пользователя", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorResponse = response.errorBody()?.string()
                    val jsonObject = JSONObject(errorResponse!!)
                    val errorMessage = jsonObject.optString("message", "Неизвестная ошибка")
                    Toast.makeText(this@UserProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun decodeBase64(base64String: String?): ByteArray? {
        return try {
            if (base64String != null) {
                Base64.decode(base64String, Base64.DEFAULT)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun handleUnauthorizedError() {
        userPreferences.logout()
        val intent = Intent(this@UserProfileActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
