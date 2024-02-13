package com.example.agtia

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var signin: Button
    private lateinit var signup: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var forgetpass: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        signin = findViewById(R.id.signin)
        signup = findViewById(R.id.sign_up)
        forgetpass =findViewById(R.id.forgetPassword)


        signup.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }


        forgetpass.setOnClickListener {
            val email = editTextEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Please check your email", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Enter your registered email", Toast.LENGTH_SHORT).show()
            }
        }

        //el btn bch ychouf est ce que mawjoud fl fire base auth el email ou non  w ya3mel el controle saisie mn louel btbia
        signin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email == "admin@gmail.com" && password == "admin123") {
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
            } else {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Sign in with email and password
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                // Check if the email is verified
                             if (currentUser?.isEmailVerified == true) {
                                    // Email is verified, allow user to sign in
                                    val intent = Intent(this, index::class.java)
                                    startActivity(intent)
                                } else {
                                    // Email is not verified, display message
                                  Toast.makeText(
                                       this,
                                       "Please verify your email address to sign in.",
                                       Toast.LENGTH_LONG
                                   ).show()
                              }
                            } else {
                                // Handle sign-in failure
                                Toast.makeText(
                                    this,
                                    "Failed to sign in: ${signInTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Fill in all the fields.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}
