package com.example.agtia.ProfileUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.databinding.ActivityMyFriendsListBinding
import com.example.agtia.todofirst.Data.MyFriendsList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class My_Friends_List_Activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var binding: ActivityMyFriendsListBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendAdapter: AdapterMyFriendsList
    private var friendList: ArrayList<MyFriendsList> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_friends_list)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("MyFriendsList")
            .child(auth.currentUser?.uid ?: "")

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        friendAdapter = AdapterMyFriendsList(friendList, databaseRef)
        recyclerView.adapter = friendAdapter


        getDataFromFirebase()
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
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
                        // For example, you can update the RecyclerView with an empty list or show a TextView
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
}
