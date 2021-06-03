package com.example.hierarchicalandroidchatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var nameObj: EditText
    private lateinit var emailObj: EditText
    private lateinit var passwordObj: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var selectPhotoButton: Button
    private lateinit var userCirclePhoto: CircleImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }

        nameObj = findViewById<EditText>(R.id.editTextTextPersonName)
        emailObj = findViewById<EditText>(R.id.editTextTextEmailLogAct)
        passwordObj = findViewById<EditText>(R.id.editTextTextPasswordLogAct)
        registerButton = findViewById<Button>(R.id.loginButtonLogAct)
        loginButton = findViewById<Button>(R.id.loginButton)
        selectPhotoButton = findViewById<Button>(R.id.selectPhotoButton)
        userCirclePhoto = findViewById<CircleImageView>(R.id.selectPhotoImageView)

        registerButton.setOnClickListener {
            performRegister()
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            userCirclePhoto.setImageBitmap(bitmap)
            selectPhotoButton.alpha = 0f

//            val bitMapDrawable = BitmapDrawable(bitmap)
//            selectPhotoButton.setBackgroundDrawable(bitMapDrawable)
        }
    }

    private fun performRegister() {
        val email: String = emailObj.text.toString()
        val password: String = passwordObj.text.toString()
        val tempPhoto: String = selectedPhotoUri.toString()

        if (email.isEmpty() || password.isEmpty() || tempPhoto.isEmpty()) {
            Toast.makeText(this, "PLease enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            uploadImageToFirebaseStorage()
        }.addOnFailureListener {
            Log.d("Main", "Failed to create user: ${it.message}")
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                saveUserToDatabase(it.toString())
            }
        }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, nameObj.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            val intent = Intent(this, LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun reload() {
        finish();
        startActivity(intent)
    }
}

@Parcelize
class User(val uid: String?, val username: String?, val profileImageUrl: String?) : Parcelable {
    constructor() : this("", "", "")
}