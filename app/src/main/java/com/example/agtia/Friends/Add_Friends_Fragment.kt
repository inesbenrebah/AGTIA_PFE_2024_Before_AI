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
import com.example.agtia.Share_Task.For_Me.Adapter_Shared_For_Me
import com.example.agtia.databinding.FragmentAddFriendsActivityBinding
import com.example.agtia.todofirst.Data.Friend
import com.example.agtia.todofirst.Data.GotDeleted
import com.example.agtia.todofirst.Data.GotFinished
import com.example.agtia.todofirst.Data.RejectedRequest
import com.example.agtia.todofirst.Data.ShareData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Add_Friends_Fragment : Fragment(), FriendRequestAdapter.RequestAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference

    private lateinit var binding: FragmentAddFriendsActivityBinding
    private lateinit var adapter: FriendRequestAdapter
    private lateinit var adapter2: FriendRequestAdapter

    private var mList: MutableList<Friend> = mutableListOf()
    private var bList: MutableList<RejectedRequest> = mutableListOf()


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
        getDataFromFirebase2()

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
        bList = mutableListOf()

        adapter = FriendRequestAdapter(mList,mutableListOf(), auth.currentUser?.email ?: "")
        adapter.setListener(this)
        databaseReference=FirebaseDatabase.getInstance().reference.child("RejectedRequest")
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.recycler2.setHasFixedSize(true)
        binding.recycler2.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bList = mutableListOf()
        adapter2 = FriendRequestAdapter(mutableListOf(), bList,auth.currentUser?.email ?: "")
        adapter2.setListener(this)
        binding.recycler2.adapter = adapter2
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

    private fun getDataFromFirebase2() {


        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            val friendListRef = FirebaseDatabase.getInstance().reference
                .child("RejectedRequest").child(encodedCurrentUserEmail)

            friendListRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val taskId = taskSnapshot.key ?: ""
                        val emailFrom = taskSnapshot.child("emailFrom").getValue(String::class.java) ?: ""
                        val emailTo = taskSnapshot.child("emailTo").getValue(String::class.java) ?: ""
                        Log.d("test","email from${emailFrom}")
                        Log.d("test","email TO${ auth.currentUser?.email}")
                        val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                        val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
                        val enco=encodeEmail(emailFrom)
                        Log.d("test","eenco ${enco}")
                        Log.d("test","eenco ${encodedCurrentUserEmail}")

                        if (enco ==encodedCurrentUserEmail) {
                            val shareData = RejectedRequest(
                                emailFrom,
                                emailTo,
                                date
                            )
                            bList.add(shareData)}}

                    adapter.notifyDataSetChanged()

                }





                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })}}

    override fun onDeleteItemClicked(friend: Friend, position: Int) {
        Log.d("salima","${friend.recipientEmail}")
        Log.d("salima","${friend.senderEmail}")
        Log.d("salima","${friend.id}")
        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val requestDlete=RejectedRequest(
        friend.senderEmail,friend.recipientEmail,formattedDate)
       databaseReference.child(encodeEmail(friend.senderEmail)).push().setValue(requestDlete)


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
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            val encodedFriendEmail = encodeEmail(friend.senderEmail)

            val currentUserFriendListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodedCurrentUserEmail)

            val friendFriendListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodedFriendEmail)

            friendFriendListRef.child(encodedCurrentUserEmail).setValue(true)
                .addOnCompleteListener { recipientTask ->
                    if (recipientTask.isSuccessful) {
                        currentUserFriendListRef.child(encodedFriendEmail).setValue(true)
                            .addOnCompleteListener { senderTask ->
                                if (senderTask.isSuccessful) {
                                    // Once both users are added to each other's friend lists, remove the friend request
                                    Toast.makeText(
                                        requireContext(),
                                        "added successfully to Your and His Friend List ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to add recipient to sender's friend list",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to add sender to recipient's friend list",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Current user email is null", Toast.LENGTH_SHORT).show()
        }
        databaseRef.child(friend.id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "removed and added ", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }

    companion object {
        private const val TAG = "Add_Friends_Fragment"
    }
}