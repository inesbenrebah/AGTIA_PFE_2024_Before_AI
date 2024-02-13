package com.example.agtia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.nio.file.FileStore

class RegisterPage : AppCompatActivity() {
        private lateinit var editTextrePassword: TextInputEditText
        private lateinit var editTextEmail: TextInputEditText
        private lateinit var editTextPassword: TextInputEditText
        private lateinit var signin: Button
        private lateinit var signup: TextView
        private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        firebaseAuth = FirebaseAuth.getInstance()



        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextrePassword = findViewById(R.id.repassword)
        signin = findViewById(R.id.signup)
        signup = findViewById(R.id.sign_up)

        signup.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        //el btn bch ychouf est ce que mawjoud fl fire base auth el email ou non  w ya3mel el controle saisie mn louel btbia
        signin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val repassword = editTextrePassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && repassword.isNotEmpty()) {
                if (password == repassword) {
                    // Create user with email and password
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val currentUser = FirebaseAuth.getInstance().currentUser

                                // Send email verification
                                currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Verification email sent. Please verify your email address.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            // Redirect user to login page after email verification
                                            val intent = Intent(this, MainActivity::class.java)

                                            val usersCollection =
                                                FirebaseFirestore.getInstance().collection("users")
                                            val user = hashMapOf(
                                                "email" to currentUser?.email
                                            )

                                            usersCollection.document(currentUser?.uid ?: "").set(user)

                                            startActivity(intent)
                                        } else {
                                            // Handle email verification sending failure
                                            Toast.makeText(
                                                this,
                                                "Failed to send verification email.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            } else {
                                // Handle user creation failure
                                Toast.makeText(
                                    this,
                                    "Failed to create user: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "The password and the confirm password don't match.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Fill in all the fields.", Toast.LENGTH_LONG).show()
            }
        }


    }
    }

