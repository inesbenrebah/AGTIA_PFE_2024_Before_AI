package com.example.agtia.Friends
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.MyAdapterEmailFriend
import com.example.agtia.todofirst.Data.Friend
import com.example.agtia.todofirst.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class AddFriends_By_Email_Activity : AppCompatActivity(), MyAdapterEmailFriend.OnDeleteClickListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapterEmailFriend: MyAdapterEmailFriend
    private lateinit var userArrayList: ArrayList<User>
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
        myAdapterEmailFriend = MyAdapterEmailFriend(userArrayList, this)
        recyclerView.adapter = myAdapterEmailFriend

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
            val usersRef = FirebaseDatabase.getInstance().reference
                .child("AllEmails")

            // Reference to MyFriendsList node
            val friendsListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodeEmail(currentUserEmail))

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tempList = mutableListOf<User>()

                    // Get the list of emails from MyFriendsList
                    friendsListRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(friendsSnapshot: DataSnapshot) {
                            for (userSnapshot in dataSnapshot.children) {
                                val user = userSnapshot.getValue(User::class.java)
                                val userEmail = user?.email
                                if (userEmail != null && userEmail != currentUserEmail) {
                                    // Check if the user's email is not in the friend list
                                    if (!friendsSnapshot.hasChild(encodeEmail(userEmail))) {
                                        tempList.add(user)
                                    } else {
                                        Log.d("Firebase", "Permission denied to access user: $userEmail")
                                    }
                                }
                            }
                            // After processing all users, filter the list
                            originalList = tempList.toList()
                            myAdapterEmailFriend.filterList(originalList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Failed to fetch user from friend list: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Log.e("Firebase", "Failed to fetch users: ${databaseError.message}")
                    Toast.makeText(
                        this@AddFriends_By_Email_Activity,
                        "Failed to fetch users: ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }



    private fun filter(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            originalList // If query is empty, show originalList
        } else {
            originalList.filter {
                it.email?.contains(query, ignoreCase = true) ?: false
            }
        }
        myAdapterEmailFriend.filterList(filteredList)
    }

    override fun sendFriendRequest(email: String) {
        val currentUserEmail = auth.currentUser?.email

        // Ensure current user is authenticated
        if (currentUserEmail != null) {
            // Proceed to send the friend request
            sendFriendRequest1(email, currentUserEmail, applicationContext)
        } else {
            Toast.makeText(this@AddFriends_By_Email_Activity, "Current user email is null", Toast.LENGTH_SHORT).show()
        }
    }



    private fun sendFriendRequest1(email: String, currentUserEmail: String, context: Context) {
        Log.d("FriendRequest", "Sending friend request to: $email")

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
                Log.d("FriendRequest", "Request sent successfully")
                Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show()
            } else {
                val errorMessage = databaseTask.exception?.message ?: "Unknown error"
                Log.e("FriendRequest", "Failed to send request: $errorMessage")
                Toast.makeText(
                    context,
                    "Failed to send request: $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }


}
