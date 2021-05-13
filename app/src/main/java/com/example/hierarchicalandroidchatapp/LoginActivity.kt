package com.example.hierarchicalandroidchatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var emailObj : EditText
    private lateinit var passwordObj : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButtonLogAct = findViewById<Button>(R.id.loginButtonLogAct)

        loginButtonLogAct.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        emailObj = findViewById<EditText>(R.id.editTextTextEmailLogAct)
        passwordObj = findViewById<EditText>(R.id.editTextTextPasswordLogAct)

        val email = emailObj.text.toString()
        val password = passwordObj.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out email and password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener

            val intent = Intent(this, LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}