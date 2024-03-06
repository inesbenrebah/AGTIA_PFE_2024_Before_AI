package com.example.agtia.Share_Task.For_Me

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.R
import com.example.agtia.Share_Task.By_Me.Adapter_Shared_By_Me
import com.example.agtia.Share_Task.Main_Share.Add_Sharing_Task
import com.example.agtia.databinding.ActivityAddSharedByMeBinding
import com.example.agtia.databinding.ActivityAddSharedForMeBinding
import com.example.agtia.todofirst.Data.ShareData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Add_Shared_For_Me_Activity  : AppCompatActivity(), Adapter_Shared_For_Me.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var binding: ActivityAddSharedForMeBinding
    private lateinit var adapter: Adapter_Shared_For_Me
    private lateinit var mList: MutableList<ShareData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSharedForMeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()



        getDataFromFirebase()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("SharedForMe").child(auth.currentUser?.uid ?: "")
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        mList = mutableListOf()
        adapter = Adapter_Shared_For_Me(mList,auth.currentUser?.email ?: "")
        adapter.setListener(this)
        binding.recycler.adapter = adapter
    }
    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }
    private fun getDataFromFirebase() {


        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            val friendListRef = FirebaseDatabase.getInstance().reference
                .child("SharedForMe")
                .child(encodedCurrentUserEmail)


            friendListRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val taskId = taskSnapshot.key ?: ""
                        val emailFrom = taskSnapshot.child("emailFrom").getValue(String::class.java) ?: ""
                        val emailTo = taskSnapshot.child("emailTo").getValue(String::class.java) ?: ""
                        val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                        val desc = taskSnapshot.child("desc").getValue(String::class.java) ?: ""
                        val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
                        val imageUri = taskSnapshot.child("imageUri").getValue(String::class.java)
                        val reminderTimeInMillis = taskSnapshot.child("reminderTime").getValue(Long::class.java) ?: -1L
                        val formattedReminderTime = formatReminderTime(reminderTimeInMillis)

                        val shareData = ShareData(
                            taskId,
                            emailFrom,
                            emailTo,
                            task,
                            desc,
                            date,
                            imageUri=imageUri,
                            reminderTime = reminderTimeInMillis,
                            formattedReminderTime = formattedReminderTime
                        )
                        mList.add(shareData)}

                    adapter.notifyDataSetChanged()
                }



                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Add_Shared_For_Me_Activity, error.message, Toast.LENGTH_SHORT).show()
                }
            })}
    }
    private fun formatReminderTime(reminderTimeInMillis: Long): String {
        if (reminderTimeInMillis == -1L) return ""
        val hours = (reminderTimeInMillis % (60 * 60 * 1000))
        val minutes = (reminderTimeInMillis % (60 * 60 * 1000)) / (60 * 1000)
        return String.format("%02d:%02d", hours, minutes)
    }

    override fun onDeleteItemClicked(shareData : ShareData, position: Int) {
        databaseRef.child(shareData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this@Add_Shared_For_Me_Activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@Add_Shared_For_Me_Activity, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(toDoData: ShareData, position: Int, context: Context) {
        val intent = Intent(context, Add_Sharing_Task::class.java)
        intent.putExtra("emailTo", toDoData.emailTo)
        intent.putExtra("taskId", toDoData.taskId)
        intent.putExtra("task", toDoData.task)
        intent.putExtra("desc", toDoData.desc)
        intent.putExtra("date", toDoData.date)
        intent.putExtra("reminderTime", toDoData.reminderTime)
        intent.putExtra("imageUri", toDoData.imageUri)
        intent.putExtra("priority", toDoData.priority)
        intent.putExtra("isEditMode", true)
        startActivity(intent)
    }

    override fun onItemClicked(toDoData: ShareData, position: Int) {
        Toast.makeText(this@Add_Shared_For_Me_Activity, "this is sent to ${toDoData.emailTo}", Toast.LENGTH_SHORT).show()
    }

}
