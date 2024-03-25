package com.example.agtia.ProfileUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.agtia.Authentication.HomeActivity
import com.example.agtia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.EmailAuthProvider


class Confirmation_with_password_activity : AppCompatActivity() {

    private lateinit var deleteAccountButton: Button
    private lateinit var cancelButton: Button
    private lateinit var passwordEditText: EditText // Add this line
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var forgetpass: TextView
    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmatuin_with_password)
        deleteAccountButton = findViewById(R.id.delete_account_button)
        cancelButton = findViewById(R.id.btn_cancel)
        passwordEditText = findViewById(R.id.editTextTextPassword) // Initialize password EditText
        forgetpass =findViewById(R.id.forgetPassword)
        forgetpass.setOnClickListener {


            currentUser?.email?.let { it1 ->
                firebaseAuth.sendPasswordResetEmail(it1)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Please check your email", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                    }
            }

        }
        cancelButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java) // Change Profile_Fragment to appropriate activity
            startActivity(intent)
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
        deleteAccountButton.setOnClickListener {
            val password = passwordEditText.text.toString() // Get entered password
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the entered password matches the current user's password
            val credential = currentUser?.email?.let { email ->
                passwordEditText.text.toString().let { password ->
                    EmailAuthProvider.getCredential(email, password)
                }
            }
            if (credential != null) {
                currentUser?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {

                        deleteAccount()
                        finish()

                    } else {
                        // Re-authentication failed
                        Toast.makeText(
                            this,
                            "Incorrect password, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

         }

    private fun deleteAccount() {
        currentUser?.let { user ->
            val userId = user.uid
            val userDocRef = db.collection("users").document(userId)
            userDocRef.delete().addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    user.delete().addOnCompleteListener { authDeleteTask ->
                        if (authDeleteTask.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Your Account Has Been Deleted. We Will Miss You.",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            val errorMessage = authDeleteTask.exception?.message ?: "Unknown error"
                            Log.e("delete", "Failed to delete user from Firebase Authentication: $errorMessage")
                            Toast.makeText(this, "Failed to delete account: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorMessage = deleteTask.exception?.message ?: "Unknown error"
                    Log.e("delete", "Failed to delete user document from Firestore: $errorMessage")
                    Toast.makeText(this, "Failed to delete user document: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}
