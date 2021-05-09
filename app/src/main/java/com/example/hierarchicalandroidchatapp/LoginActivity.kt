package com.example.hierarchicalandroidchatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var emailObj : EditText
    private lateinit var passwordObj : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailObj = findViewById<EditText>(R.id.editTextTextEmailLogAct)
        passwordObj = findViewById<EditText>(R.id.editTextTextPasswordLogAct)

        val loginButtonLogAct = findViewById<Button>(R.id.loginButtonLogAct)

        loginButtonLogAct.setOnClickListener {
            val email = emailObj.text.toString()
            val password = passwordObj.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {

            }
        }

        loginButtonLogAct.setOnClickListener {
            finish()
        }
    }
}