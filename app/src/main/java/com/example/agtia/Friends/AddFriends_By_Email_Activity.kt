package com.example.agtia.Friends
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.databinding.ActivityAddFreindByEmailBinding
import com.example.agtia.MyAdapterEmailFriend

import com.example.agtia.todofirst.Data.Friend
import com.example.agtia.todofirst.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class AddFriends_By_Email_Activity : AppCompatActivity(), MyAdapterEmailFriend.OnDeleteClickListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var MyAdapterEmailFriend: MyAdapterEmailFriend
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    private lateinit var originalList: List<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friend_activity_email_like_admin)
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.search)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()
        MyAdapterEmailFriend = MyAdapterEmailFriend(userArrayList, this)
        recyclerView.adapter = MyAdapterEmailFriend

        fetchUsersFromFirestore()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }
    private fun fetchUsersFromFirestore() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserEmail != null) {
            val usersRef = FirebaseFirestore.getInstance().collection("users")

            usersRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                originalList = querySnapshot.toObjects(User::class.java)

                // Encode emails in the original list
                val encodedOriginalList = originalList.map { user ->
                    user.copy(email = encodeEmail(user.email ?: ""))
                }

                // Exclude your own email from the list
                val filteredList = encodedOriginalList.filter { user ->
                    user.email != encodeEmail(currentUserEmail)
                }

                // Fetch the list of friends for the current user from MyFriendsList
                val currentUserFriendsRef = FirebaseDatabase.getInstance().reference
                    .child("MyFriendsList")
                    .child(currentUserEmail?.let { encodeEmail(it) }.toString())

                currentUserFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val currentUserFriendsList = mutableListOf<String>()

                        for (friendSnapshot in dataSnapshot.children) {
                            friendSnapshot.key?.let { currentUserFriendsList.add(it) }
                        }

                        // Filter out the emails already in the MyFriendsList
                        val finalFilteredList = filteredList.filter { user ->
                            encodeEmail(user.email ?: "") !in currentUserFriendsList
                        }
                        originalList = finalFilteredList

                        // Set the filtered list to the adapter
                        MyAdapterEmailFriend.filterList(finalFilteredList)

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                        Toast.makeText(
                            this@AddFriends_By_Email_Activity,
                            "Failed to fetch friends: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to fetch users: ${exception.message}", Toast.LENGTH_SHORT).show()
        }}
    }



    private fun filter(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            originalList // If query is empty, show originalList
        } else {
            originalList.filter {
                it.email?.contains(query, ignoreCase = true) ?: false
            }
        }
        MyAdapterEmailFriend.filterList(filteredList)
    }


    override fun sendFriendRequest(email: String) {
        val currentUserEmail = auth.currentUser?.email

        // Ensure current user is authenticated
        if (currentUserEmail != null) {

            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("users")

            // Check if the entered email exists in the list of users
            usersRef.whereEqualTo("email", email).get().addOnSuccessListener { userQuerySnapshot ->

                    // User with the entered email exists
                    val friendListRef = FirebaseDatabase.getInstance().reference
                        .child("MyFriendsList")
                        .child(encodeEmail(email))

                    // Check if the current user exists in the friend's friend list
                    friendListRef.child(encodeEmail(currentUserEmail)).get()
                        .addOnCompleteListener { friendCheckTask ->
                            if (friendCheckTask.isSuccessful) {
                                val friendExists = friendCheckTask.result?.value != null
                                if (friendExists) {
                                    // User is already in the friend's friend list
                                    Toast.makeText(
                                        this,
                                        "User is already in the friend's friend list",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Proceed to send the friend request
                                    sendFriendRequest1(email, currentUserEmail, context =applicationContext)
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to check friend's friend list: ${friendCheckTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to check user: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, "Current user email is null", Toast.LENGTH_SHORT).show()
        }


    }

    private fun sendFriendRequest1(email: String, currentUserEmail: String, context: Context) {
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
                Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Failed to send request: ${databaseTask.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }


}
