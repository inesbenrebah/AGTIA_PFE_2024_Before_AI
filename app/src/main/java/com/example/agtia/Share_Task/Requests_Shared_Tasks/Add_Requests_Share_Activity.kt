package com.example.agtia.Share_Task.Requests_Shared_Tasks

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide.init
import com.example.agtia.R
import com.example.agtia.databinding.ActivityAddRequestsShareBinding
import com.example.agtia.todofirst.Data.ShareData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlin.math.log

class Add_Requests_Share_Activity : AppCompatActivity(), Request_Tasks_Adapter.ToDoAdapterClicksInterface {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var binding: ActivityAddRequestsShareBinding
    private lateinit var adapter: Request_Tasks_Adapter
    private lateinit var mList: MutableList<ShareData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRequestsShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        getDataFromFirebase()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("ShareData")
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        mList = mutableListOf()
        adapter = Request_Tasks_Adapter(mList, auth.currentUser?.email ?: "")
        adapter.setListener(this)
        binding.recycler.adapter = adapter
    }
    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }

  override  fun AcceptRequest(toDoData: ShareData,emailTo:String, position: Int, context: Context) {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            val encodedFriendEmail = encodeEmail(toDoData.emailTo)

            val taskId = FirebaseDatabase.getInstance().reference.child("AcceptedTasks").push().key ?: ""


            // Save the task data for current user
            val currentUserTaskRef = FirebaseDatabase.getInstance().reference.child("AcceptedTasks").child(encodedCurrentUserEmail).child(taskId)
            val friendUserTaskRef = FirebaseDatabase.getInstance().reference.child("RequestShared").child(encodedFriendEmail).child(taskId)


            currentUserTaskRef.setValue(toDoData).addOnCompleteListener { currentUserTaskTask ->
                    if (currentUserTaskTask.isSuccessful) {
                        // Save the task data for friend user
                        Log.d("nosnos","this is the current user ${encodedCurrentUserEmail}, and this saved as ${currentUserTaskRef}")
                        friendUserTaskRef.setValue(toDoData).addOnCompleteListener { friendUserTaskTask ->
                                if (friendUserTaskTask.isSuccessful) {
                                    Log.d("nosnos","this is the friend user ${encodedFriendEmail}, and this saved as ${friendUserTaskRef}")

                                    // Remove the task from the list and notify adapter
                                    mList.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                    Toast.makeText(context, "Friend Accepted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to accept task for friend user", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Failed to accept task for current user", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Current user email is null", Toast.LENGTH_SHORT).show()
        }
    }


    override fun RejectRequest(toDoData: ShareData, position: Int) {
        val taskId = toDoData.taskId
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("ShareData")
            .child(taskId)

        // Remove the task from the database
        databaseRef.removeValue()
            .addOnCompleteListener { removeTask ->
                if (removeTask.isSuccessful) {
                    // Remove the task from the list and notify adapter
                    mList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    Toast.makeText(this, "You Rejected The Shared task", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to remove shared task", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key ?: ""
                    val emailFrom = taskSnapshot.child("emailFrom").getValue(String::class.java) ?: ""
                    Log.d("try","this is the emailfrom ${emailFrom}")
                    val emailTo = taskSnapshot.child("emailTo").getValue(String::class.java) ?: ""
                    Log.d("try","this is the emailTo ${emailTo}")

                    val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                    val desc = taskSnapshot.child("desc").getValue(String::class.java) ?: ""
                    val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
                    val imageUri = taskSnapshot.child("imageUri").getValue(String::class.java)
                    val userPhotoUrl = taskSnapshot.child("userPhotoUrl").getValue(String::class.java)
                    val reminderTimeInMillis = taskSnapshot.child("reminderTime").getValue(Long::class.java) ?: -1L
                    val formattedReminderTime = formatReminderTime(reminderTimeInMillis)
                    if (emailTo == auth.currentUser?.email) {
                        val shareData = ShareData(
                            taskId,
                            emailFrom,
                            emailTo,
                            task,
                            desc,
                            date,
                            imageUri = imageUri,
                            userPhotoUrl = userPhotoUrl,
                            reminderTime = reminderTimeInMillis,
                            formattedReminderTime = formattedReminderTime
                        )
                        mList.add(shareData)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Requests_Share_Activity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatReminderTime(reminderTimeInMillis: Long): String {
        if (reminderTimeInMillis == -1L) return ""
        val hours = (reminderTimeInMillis % (60 * 60 * 1000))
        val minutes = (reminderTimeInMillis % (60 * 60 * 1000)) / (60 * 1000)
        return String.format("%02d:%02d", hours, minutes)
    }
}
