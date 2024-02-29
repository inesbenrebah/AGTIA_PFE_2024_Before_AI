package com.example.agtia.Friends
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.agtia.R
import com.example.agtia.databinding.ActivityAddFreindByEmailBinding
import com.example.agtia.todofirst.Data.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AddFriends_By_Email_Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAddFreindByEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFreindByEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupViews()
    }

    private fun setupViews() {
        binding.todoClose.setOnClickListener { finish() }
        binding.todoNextBtn.setOnClickListener { onSaveRequest() }
    }

    private fun onSaveRequest() {
        val email = binding.FriendEmail.text.toString()
        if (email.isNotEmpty()) {
            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail != null) {
                val db = FirebaseFirestore.getInstance()
                val usersRef = db.collection("users")

                // Check if the entered email exists in the list of users
                usersRef.whereEqualTo("email", email).get().addOnSuccessListener { userQuerySnapshot ->
                    if (!userQuerySnapshot.isEmpty) {
                        // User with the entered email exists
                        val friendListRef = FirebaseDatabase.getInstance().reference
                            .child("MyFriendsList")
                            .child(encodeEmail(email))

                        // Check if the current user exists in the friend's friend list
                        friendListRef.child(encodeEmail(currentUserEmail)).get().addOnCompleteListener { friendCheckTask ->
                            if (friendCheckTask.isSuccessful) {
                                val friendExists = friendCheckTask.result?.value != null
                                if (friendExists) {
                                    // User is already in the friend's friend list
                                    Toast.makeText(this, "User is already in the friend's friend list", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Proceed to send the friend request
                                    sendFriendRequest(email, currentUserEmail)
                                }
                            } else {
                                Toast.makeText(this, "Failed to check friend's friend list: ${friendCheckTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // User with the entered email does not exist
                        Toast.makeText(this, "User with this email does not exist", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to check user: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Current user email is null", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter friend's email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendFriendRequest(email: String, currentUserEmail: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("Friend")
        val newRequestRef = databaseRef.push()
        val newRequestId = newRequestRef.key ?: ""

        // Create Friend object with the request details
        val requestFriend = Friend(
            id = newRequestId,
            senderEmail = currentUserEmail,
            recipientEmail = email,
            status = Friend.STATUS_PENDING
        )

        // Store the friend request in the database
        val requestMap = requestFriend.toMap()
        newRequestRef.setValue(requestMap).addOnCompleteListener { databaseTask ->
            if (databaseTask.isSuccessful) {
                Toast.makeText(this, "Request sent successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to send request: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }
}
