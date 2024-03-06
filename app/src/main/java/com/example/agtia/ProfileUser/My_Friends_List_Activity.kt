package com.example.agtia.ProfileUser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.databinding.ActivityMyFriendsListBinding
import com.example.agtia.todofirst.Data.MyFriendsList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.agtia.ProfileUser.AdapterMyFriendsList

class My_Friends_List_Activity : AppCompatActivity(), AdapterMyFriendsList.OnRemoveClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var binding: ActivityMyFriendsListBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendAdapter: AdapterMyFriendsList
    private var friendList: ArrayList<MyFriendsList> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFriendsListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("MyFriendsList")
        recyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        friendAdapter = AdapterMyFriendsList(friendList, this)
        recyclerView.adapter = friendAdapter

        getDataFromFirebase()
    }
    private fun deleteUserByEmail(email: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.email
        Log.d("removeFriendFromDatabase", "User ID: $userId")
        if (userId != null) {
            // Encode userId
            val encodedUserId = encodeEmail(userId)

            // Remove friend from the database using their email address
            val encodedEmail = encodeEmail(email)
            Log.d("removeFriendFromDatabase", "Encoded email: $encodedEmail")

            databaseRef.child(encodedUserId).child(encodedEmail).removeValue()
                .addOnSuccessListener {
                    Log.d("removeFriendFromDatabase", "Friend removed successfully from database")
                    // Remove the friend from the local list once removed from database
                    val position = friendList.indexOfFirst { it.email == email }
                    Log.d("removeFriendFromDatabase", "Friend position: $position")
                    if (position != -1) {
                        friendList.removeAt(position)
                        friendAdapter.notifyItemRemoved(position)
                        Toast.makeText(this, "Friend removed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("removeFriendFromDatabase", "Failed to remove friend: ${e.message}")
                    Toast.makeText(this, "Failed to remove friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }

    private fun getDataFromFirebase() {
        val currentUserEmail = auth.currentUser?.email
        Log.d("getDataFromFirebase", "Current user email: $currentUserEmail")
        if (currentUserEmail != null) {
            // Check if the current user has any friends
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            Log.d("getDataFromFirebase", "Encoded current user email: $encodedCurrentUserEmail")

            val friendListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodedCurrentUserEmail)

            friendListRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("getDataFromFirebase", "Data change event triggered")
                    // Clear the current list
                    friendList.clear()

                    if (dataSnapshot.exists()) {
                        // User has friends, retrieve and display the friend list
                        for (friendSnapshot in dataSnapshot.children) {
                            val friendEmail = friendSnapshot.key ?: "N/A"
                            Log.d("getDataFromFirebase", "Friend email: $friendEmail")
                            val friend = MyFriendsList(friendEmail)
                            friendList.add(friend)
                        }
                    } else {
                        // User doesn't have any friends, display a message or show a placeholder UI
                        Log.d("getDataFromFirebase", "No friends found")
                        showNoFriendsMessage()
                    }

                    // Notify the adapter that the data set has changed
                    friendAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("getDataFromFirebase", "Database Error: ${databaseError.message}")
                }
            })
        } else {
            // Handle the case where the current user's email is null
            Log.d("getDataFromFirebase", "Current user email is null")
            Toast.makeText(this, "Current user email is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoFriendsMessage() {
        val intent = Intent(this, NoFriends::class.java)
        startActivity(intent)
    }

    override fun onRemoveClick(email: String) {
        deleteUserByEmail(email)
    }
}

