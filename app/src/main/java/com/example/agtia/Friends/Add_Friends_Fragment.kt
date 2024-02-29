package com.example.agtia.Friends


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.databinding.FragmentAddFriendsActivityBinding
import com.example.agtia.todofirst.Data.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Add_Friends_Fragment : Fragment(), FriendRequestAdapter.RequestAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var binding: FragmentAddFriendsActivityBinding
    private lateinit var adapter: FriendRequestAdapter
    private var mList: MutableList<Friend> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFriendsActivityBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        init()
        initRecyclerView()
        registerEvent()
        getDataFromFirebase()
        return binding.root
    }

    private fun registerEvent() {
        binding.addBtnHome.setOnClickListener {
            val intent = Intent(requireActivity(), AddFriends_By_Email_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        // Remove currentUser UID from the reference
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Friend") // Remove .child(auth.currentUser?.uid.toString())

        mList = mutableListOf()
        adapter = FriendRequestAdapter(mList, auth.currentUser?.email ?: "")
        adapter.setListener(this)

        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
    }


    private fun initRecyclerView() {
        binding.recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mList.clear()
                for (snapshot in dataSnapshot.children) {
                    val id=snapshot.child("id").value.toString()
                    val senderEmail = snapshot.child("senderEmail").value.toString()
                    val recipientEmail = snapshot.child("recipientEmail").value.toString()
                    val status = snapshot.child("status").value.toString()
                    if (recipientEmail == auth.currentUser?.email) { // Filter based on recipient's email
                        val friend = Friend(id, senderEmail, recipientEmail, status)
                        mList.add(friend)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException())
            }
        })
    }



    override fun onDeleteItemClicked(friend: Friend, position: Int) {
        databaseRef.child(friend.id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onItemClicked(friend: Friend) {
        // for item click
    }


    override fun AddToMyFriendList(friend: Friend, position: Int) {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            // Encode current user's email address
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)

            // Encode friend's email address
            val encodedFriendEmail = encodeEmail(friend.senderEmail)

            // Reference to current user's friend list
            val currentUserFriendListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodedCurrentUserEmail)

            // Reference to friend's friend list
            val friendFriendListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodedFriendEmail)

            // Add sender to recipient's friend list
            friendFriendListRef.child(encodedCurrentUserEmail).setValue(true).addOnCompleteListener { recipientTask ->
                    if (recipientTask.isSuccessful) {
                        // Add recipient to sender's friend list
                        currentUserFriendListRef.child(encodedFriendEmail).setValue(true).addOnCompleteListener { senderTask ->
                                if (senderTask.isSuccessful) {
                                    // Remove friend request from Firebase
                                    databaseRef.child(friend.id).removeValue()
                                        .addOnCompleteListener { removeTask ->
                                            if (removeTask.isSuccessful) {
                                                // Remove friend from the list
                                                mList.removeAt(position)
                                                adapter.notifyItemRemoved(position)
                                                Toast.makeText(context, "Friend Accepted", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to remove friend request", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Failed to add recipient to sender's friend list", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Failed to add sender to recipient's friend list", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Handle the case where current user email is null
            Toast.makeText(context, "Current user email is null", Toast.LENGTH_SHORT).show()
        }



        // Remove friend request from Firebase
        databaseRef.child(friend.id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Friend Accepted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

        // Remove friend from the list
        mList.removeAt(position)
        adapter.notifyItemRemoved(position)
        Toast.makeText(context, "Friend Accepted ", Toast.LENGTH_SHORT).show()




    }
    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }

    companion object {
        private const val TAG = "Add_Friends_Fragment"
    }
}