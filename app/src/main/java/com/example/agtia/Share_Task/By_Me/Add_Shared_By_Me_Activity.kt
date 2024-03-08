package com.example.agtia.Share_Task.By_Me

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.Share_Task.Main_Share.Add_Sharing_Task
import com.example.agtia.databinding.ActivityAddSharedByMeBinding
import com.example.agtia.todofirst.Data.GotDeleted
import com.example.agtia.todofirst.Data.GotFinished
import com.example.agtia.todofirst.Data.ShareData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Add_Shared_By_Me_Activity : AppCompatActivity(), Adapter_Shared_By_Me.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef1: DatabaseReference
    private lateinit var databaseRef2: DatabaseReference
    private lateinit var  databaseReference:DatabaseReference
    private lateinit var binding: ActivityAddSharedByMeBinding
    private lateinit var adapter: Adapter_Shared_By_Me
    private lateinit var adapter2:Adapter_Shared_By_Me
    private lateinit var mList: MutableList<ShareData>
    private lateinit var bList: MutableList<GotFinished>

    private lateinit var functions: FirebaseFunctions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSharedByMeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        functions = FirebaseFunctions.getInstance()

        getDataFromFirebase()
        getDataFromFirebase2()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()

        databaseReference = FirebaseDatabase.getInstance().reference

        databaseRef = FirebaseDatabase.getInstance().reference.child("SharedByMe").child(auth.currentUser?.uid ?: "")
        databaseRef1 = FirebaseDatabase.getInstance().reference.child("SharedForMe")
        databaseRef2 = FirebaseDatabase.getInstance().reference.child("SharedByMe")


        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        mList = mutableListOf()
        adapter = Adapter_Shared_By_Me(mList, mutableListOf())
        adapter.setListener(this)
        binding.recycler.adapter = adapter

        binding.recycler2.setHasFixedSize(true)
        binding.recycler2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bList = mutableListOf()
        adapter2 = Adapter_Shared_By_Me(mutableListOf(), bList)
        adapter2.setListener(this)
        binding.recycler2.adapter = adapter2
    }


    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }  private fun decodeEmail(email: String): String {
        return email.replace("-", ".")
    }
    private fun getDataFromFirebase() {


        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            val friendListRef = FirebaseDatabase.getInstance().reference
                .child("SharedByMe").child(encodedCurrentUserEmail)

            friendListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key ?: ""
                    val emailFrom = taskSnapshot.child("emailFrom").getValue(String::class.java) ?: ""
                    val emailTo = taskSnapshot.child("emailTo").getValue(String::class.java) ?: ""
                    Log.d("test","email from${emailFrom}")
                    Log.d("test","email TO${ auth.currentUser?.email}")
                    val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                    val desc = taskSnapshot.child("desc").getValue(String::class.java) ?: ""
                    val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
                    val imageUri = taskSnapshot.child("imageUri").getValue(String::class.java)
                    val reminderTimeInMillis = taskSnapshot.child("reminderTime").getValue(Long::class.java) ?: -1L
                    val formattedReminderTime = formatReminderTime(reminderTimeInMillis)
                    if (emailFrom ==encodedCurrentUserEmail) {
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
                    mList.add(shareData)}}

                adapter.notifyDataSetChanged()

            }



            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Shared_By_Me_Activity, error.message, Toast.LENGTH_SHORT).show()
            }
        })}
    }


    private fun getDataFromFirebase2() {


        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            val encodedCurrentUserEmail = encodeEmail(currentUserEmail)
            val friendListRef = FirebaseDatabase.getInstance().reference
                .child("GotFinished").child(encodedCurrentUserEmail)

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
                        val enco=encodeEmail(emailTo)
                        Log.d("test","eenco ${enco}")
                        Log.d("test","eenco ${encodedCurrentUserEmail}")

                        if (enco ==encodedCurrentUserEmail) {
                            val shareData = GotFinished(
                                taskId,
                                emailFrom,
                                emailTo,
                                task,
                                date
                            )
                            bList.add(shareData)}}

                    adapter.notifyDataSetChanged()

                }



                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Add_Shared_By_Me_Activity, error.message, Toast.LENGTH_SHORT).show()
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
        Log.d("removee nosnos","removveeddd")
        val tr =shareData.taskId

        val currentuser=auth.currentUser?.email
        val encodedCurrentUserEmail = currentuser?.let { encodeEmail(it) }
        Log.d("removee nosnos","inti ${currentuser} w hedha coded ${encodedCurrentUserEmail}")
        Log.d("removee nosnos","w hedhi el id taa task${shareData.emailFrom}")
       sendDeletedTask(shareData)

        if (encodedCurrentUserEmail != null) {
            databaseRef2.child(encodedCurrentUserEmail).child(shareData.emailFrom).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {

                    Toast.makeText(this@Add_Shared_By_Me_Activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Add_Shared_By_Me_Activity, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        databaseRef1.child(tr).child(shareData.emailFrom).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("removee nosnos","theb tnahi mn and ${tr} el task ${shareData.emailFrom}")

                val decode =tr?.let { decodeEmail(it) }
                sendEmail(decode.toString(),encodedCurrentUserEmail.toString())

                Toast.makeText(this@Add_Shared_By_Me_Activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@Add_Shared_By_Me_Activity, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun sendEmail(email: String,ts:String) {
        val data = hashMapOf(
            "email" to email,
            "text" to "${ts} has Deleted a task that He Sended To You."
        )

        functions
            .getHttpsCallable("sendEmailOnTaskDeletion")
            .call(data)
            .addOnSuccessListener { result ->
                Log.d("email", "Email sent successfully: ${result.data}")
            }
            .addOnFailureListener { e ->
                Log.e("email", "Error sending email", e)
            }
    }


private fun sendDeletedTask(toDoData: ShareData){
    val taskId=toDoData.emailTo
    val otheeruser =toDoData.emailFrom
    val newTaskRef=databaseReference.child("GotDeleted").child(toDoData.taskId).push()
    val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))


    val currentuser=auth.currentUser?.email
    val encodedCurrentUserEmail = currentuser?.let { encodeEmail(it) }
    val newDelete= GotDeleted(
        emailFrom = encodedCurrentUserEmail.toString(),
        emailTo = toDoData.taskId,
        taskId = toDoData.emailFrom,
        task=toDoData.task,
        date=formattedDate

    )
    newTaskRef.setValue(newDelete).addOnCompleteListener {
        if (it.isSuccessful) {
            Toast.makeText(this, "Task added to GotDeleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
        }
    }

}
    override fun onItemClicked(toDoData: ShareData, position: Int) {
        Toast.makeText(this@Add_Shared_By_Me_Activity, "this is sent to ${toDoData.emailTo}", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClicked2(toDoData: GotFinished, position: Int) {
        Toast.makeText(this@Add_Shared_By_Me_Activity, " ${toDoData.emailTo} Has Deleted The task", Toast.LENGTH_SHORT).show()
    }

}
