package com.example.protypeapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.protypeapp.controller.listeners.UserProfileListener
import com.example.protypeapp.controller.UserProfileController
import com.example.protypeapp.models.user.UserInfo
import com.example.protypeapp.userStorage.UserPreferences
import com.example.protypeapp.utils.Utils

class UserProfileActivity : AppCompatActivity(), UserProfileListener {
    private lateinit var image: ImageView
    private lateinit var textFio: EditText
    private lateinit var textEmail: EditText
    private lateinit var editButton: Button
    private lateinit var exitButton: Button
    private lateinit var userPreferences: UserPreferences
    private lateinit var controller: UserProfileController

    private var initialFio: String = ""
    private var initialEmail: String = ""
    private var isImageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        setupViews()
        setupListeners()

        userPreferences = UserPreferences(this)
        controller = UserProfileController(this, this)
        controller.fetchUserInfo()
        controller.checkAuthorization()
    }

    private fun setupViews() {
        image = findViewById(R.id.ivProfileImage)
        textFio = findViewById(R.id.etFullName)
        textEmail = findViewById(R.id.etEmail)
        editButton = findViewById(R.id.btnEdit)
        exitButton = findViewById(R.id.btnExit)
    }

    private fun setupListeners() {
        setupChangeListeners()

        editButton.setOnClickListener {
            updateUser()
        }

        exitButton.setOnClickListener {
            userPreferences.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }
    }

    private fun setupChangeListeners() {
        textFio.addTextChangedListener { checkForChanges() }
        textEmail.addTextChangedListener { checkForChanges() }
    }

    private fun checkForChanges() {
        editButton.isEnabled = textFio.text.toString() != initialFio ||
                textEmail.text.toString() != initialEmail || isImageChanged
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
                    showToast("Не удалось загрузить изображение")
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
    }

    private fun updateUser() {
        val fioText = textFio.text.toString().trim()
        val imageBitmap = (image.drawable as? BitmapDrawable)?.bitmap
        val email = textEmail.text.toString()

        showPasswordDialog { password ->
            controller.updateUser(fioText, email, imageBitmap, password)
        }
    }

    private fun showPasswordDialog(onPasswordEntered: (String) -> Unit) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("Введите пароль")
            .setView(input)
            .setPositiveButton("ОК") { _, _ -> onPasswordEntered(input.text.toString()) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onUserInfoReceived(user: UserInfo) {
        initialFio = "${user.surname} ${user.name} ${user.patronymic}"
        initialEmail = user.email

        textFio.setText(initialFio)
        textEmail.setText(initialEmail)

        if (user.image != null && user.image != "none") {
            val imageBytes = Utils.decodeBase64(user.image)
            image.setImageBitmap(imageBytes)
        } else {
            image.setImageResource(R.drawable.person)
        }
    }

    override fun onUserImageReceived(image: Bitmap?) {
        image?.let { this.image.setImageBitmap(it) }
            ?: this.image.setImageResource(R.drawable.person)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun onUnauthorized() {
        startActivity(Intent(this@UserProfileActivity, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
