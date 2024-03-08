package com.example.agtia.Share_Task.For_Me

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.R
import com.example.agtia.Share_Task.By_Me.Adapter_Shared_By_Me
import com.example.agtia.Share_Task.Main_Share.Add_Sharing_Task
import com.example.agtia.databinding.ActivityAddSharedByMeBinding
import com.example.agtia.databinding.ActivityAddSharedForMeBinding
import com.example.agtia.todofirst.Data.GotFinished
import com.example.agtia.todofirst.Data.History
import com.example.agtia.todofirst.Data.ShareData
import com.example.agtia.todofirst.Data.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Add_Shared_For_Me_Activity  : AppCompatActivity(), Adapter_Shared_For_Me.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseReff: DatabaseReference
    private lateinit var databaseRefff: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseRef1:DatabaseReference
    private lateinit var functions: FirebaseFunctions
    private lateinit var binding: ActivityAddSharedForMeBinding
    private lateinit var adapter: Adapter_Shared_For_Me
    private lateinit var mList: MutableList<ShareData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSharedForMeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        functions = FirebaseFunctions.getInstance()

        getDataFromFirebase()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("SharedForMe").child(auth.currentUser?.uid ?: "")
        binding.recycler.setHasFixedSize(true)

        databaseReff = FirebaseDatabase.getInstance().reference.child("SharedForMe")
        databaseRefff = FirebaseDatabase.getInstance().reference.child("SharedByMe")
        databaseReference=FirebaseDatabase.getInstance().reference

        databaseRef1 = FirebaseDatabase.getInstance().reference
            .child("History").child(auth.currentUser?.uid.toString())

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
                .child("SharedForMe").child(encodedCurrentUserEmail)



            friendListRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val taskId = taskSnapshot.key ?: ""
                        val emailFrom =
                            taskSnapshot.child("emailFrom").getValue(String::class.java) ?: ""

                        val emailTo =
                            taskSnapshot.child("emailTo").getValue(String::class.java) ?: ""
                        Log.d("try2", "this is the emailto ${emailTo}")
                        Log.d("try2", "this is the emailencoded ${encodedCurrentUserEmail}")
                        val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                        val desc = taskSnapshot.child("desc").getValue(String::class.java) ?: ""
                        val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
                        val imageUri = taskSnapshot.child("imageUri").getValue(String::class.java)
                        val reminderTimeInMillis =
                            taskSnapshot.child("reminderTime").getValue(Long::class.java) ?: -1L
                        val formattedReminderTime = formatReminderTime(reminderTimeInMillis)
                        if (encodeEmail(emailTo) == encodedCurrentUserEmail) {
                            val shareData = ShareData(
                                taskId,
                                emailFrom,
                                emailTo,
                                task,
                                desc,
                                date,
                                imageUri = imageUri,
                                reminderTime = reminderTimeInMillis,
                                formattedReminderTime = formattedReminderTime
                            )
                            mList.add(shareData)
                        }
                    }
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

    override fun onDissatisfiedIconClicked(toDoData: ShareData, position: Int) {
        // Change the icon immediately
        adapter.notifyItemChanged(position)


        //send that finished the Task
        FinishedTask(toDoData)



        // Use Handler to delay the save and delete operations
        Handler(Looper.getMainLooper()).postDelayed({
            // Remove the task from the list
            mList.removeAt(position)
            adapter.notifyItemRemoved(position)

            // Save the task to history
            saveTaskToHistory(toDoData)
            // Delete the task from Firebase
            deleteTaskFromFirebase(toDoData, position)

            Toast.makeText(this, "Task Completed successfully", Toast.LENGTH_SHORT).show()
        }, 2000) // Delay of 2 seconds
    }


    private fun decodeEmail(email: String): String {
        return email.replace("-", ".")
    }
    private fun deleteTaskFromFirebase(shareData : ShareData, position: Int) {


        Log.d("his nosnos","finished")
        val tr =shareData.taskId

        val currentuser=auth.currentUser?.email
        val encodedCurrentUserEmail = currentuser?.let { encodeEmail(it) }
        Log.d("his nosnos","inti ${currentuser} w hedha coded ${encodedCurrentUserEmail}")
        Log.d("his nosnos","w hedhi el id taa task${shareData.emailFrom}")
        if (encodedCurrentUserEmail != null) {
        databaseReff.child(encodedCurrentUserEmail).child(shareData.emailFrom).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Task Completed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }}

        databaseRefff.child(shareData.emailTo).child(shareData.emailFrom).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("his nosnos","theb tnahi mn and ${tr} el task ${shareData.emailFrom}")


                val decode = shareData.emailTo?.let { decodeEmail(it) }
                decode?.let { decodedEmail ->
                    sendEmail(decodedEmail, currentuser.toString())
                }
                Toast.makeText(this@Add_Shared_For_Me_Activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@Add_Shared_For_Me_Activity ,it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            }

        }
    }

    private fun sendEmail(email: String, ts: String) {
        val data = hashMapOf(
            "email" to email,
            "text" to "$ts has finished a task that He Sent To You."
        )

        functions
            .getHttpsCallable("sendEmailOnTaskDeletion")
            .call(data)
            .addOnSuccessListener { result ->
                Log.d("email", "Email sent successfully: ${result.data}")
            }
            .addOnFailureListener { e ->
                Log.e("email", "Error sending email", e)
                Toast.makeText(this@Add_Shared_For_Me_Activity, "Error sending email", Toast.LENGTH_SHORT).show()
            }
    }
    private fun FinishedTask(toDoData: ShareData) {
        val taskId=toDoData.emailTo
        val newTaskRef=databaseReference.child("GotFinished").child(taskId).push()
        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val otheeruser =toDoData.emailFrom

        val currentuser=auth.currentUser?.email
        val encodedCurrentUserEmail = currentuser?.let { encodeEmail(it) }
        val newFinish=GotFinished(
            emailFrom = encodedCurrentUserEmail.toString(),
            emailTo = toDoData.taskId,
            taskId = toDoData.emailFrom,
            task=toDoData.task,
            date=formattedDate

        )
        newTaskRef.setValue(newFinish).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Task added to Finish successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun saveTaskToHistory(toDoData: ShareData) {
        val newTaskRef = databaseRef1.push()
        val taskId = newTaskRef.key ?: ""
        val newTaskHistory = History(
            taskId = taskId,
            task = toDoData.task,
            desc = toDoData.desc,
            date = toDoData.date,
            imageUri = toDoData.imageUri,
            priority = toDoData.priority,
            reminderTime = toDoData.reminderTime
        )

        newTaskRef.setValue(newTaskHistory).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Task added to history successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
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
