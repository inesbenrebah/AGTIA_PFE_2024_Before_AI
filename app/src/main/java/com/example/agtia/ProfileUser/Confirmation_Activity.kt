package com.example.agtia.ProfileUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.agtia.Authentication.HomeActivity
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
           val intent=Intent(this ,Confirmation_with_password_activity::class.java)
            startActivity(intent)
            finish()
        }



        cancelButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java) // Change Profile_Fragment to appropriate activity
            startActivity(intent)
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
