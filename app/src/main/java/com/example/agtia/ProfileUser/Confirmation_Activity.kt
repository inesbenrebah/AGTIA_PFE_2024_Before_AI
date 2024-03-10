package com.example.agtia.ProfileUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.agtia.AddTask.My_ToDoList_Fragment
import com.example.agtia.Authentication.HomeActivity
import com.example.agtia.Authentication.Log_In_Activity
import com.example.agtia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Confirmation_Activity : AppCompatActivity() {
    private lateinit var deleteAccountButton: Button
    private lateinit var cancelButton: Button
    private val currentUser = FirebaseAuth.getInstance().currentUser
private val db=FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        deleteAccountButton = findViewById(R.id.delete_account_button)
        cancelButton = findViewById(R.id.btn_cancel)

        deleteAccountButton.setOnClickListener {
            currentUser?.let { user ->
                Log.d("delete", "UID: ${user.uid}, Email: ${user.email}")

                val userId = user.uid
                val userDocRef = db.collection("users").document(userId)
                userDocRef.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        // User document deleted successfully from Firestore, now delete from Firebase Authentication
                        user.delete().addOnCompleteListener { authDeleteTask ->
                            if (authDeleteTask.isSuccessful) {
                                Toast.makeText(this, "Your Account Has Been Deleted. We Will Miss You.", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                // Failed to delete user from Firebase Authentication
                                val errorMessage = authDeleteTask.exception?.message ?: "Unknown error"
                                Log.e("delete", "Failed to delete user from Firebase Authentication: $errorMessage")
                                Toast.makeText(this, "Failed to delete account: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Failed to delete user document from Firestore
                        val errorMessage = deleteTask.exception?.message ?: "Unknown error"
                        Log.e("delete", "Failed to delete user document from Firestore: $errorMessage")
                        Toast.makeText(this, "Failed to delete user document: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        cancelButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java) // Change Profile_Fragment to appropriate activity
            startActivity(intent)
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
