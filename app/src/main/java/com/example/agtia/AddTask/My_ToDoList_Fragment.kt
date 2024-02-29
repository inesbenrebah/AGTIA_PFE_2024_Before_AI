package com.example.agtia.AddTask

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.databinding.FragmentTodolistBinding
import com.example.agtia.todofirst.Data.History
import com.example.agtia.todofirst.Data.Priority
import com.example.agtia.todofirst.Data.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class My_ToDoList_Fragment : Fragment(), Add_ToDoList_Adapter.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef1: DatabaseReference
    private lateinit var binding: FragmentTodolistBinding

    private lateinit var adapter: Add_ToDoList_Adapter
    private lateinit var mList: MutableList<ToDoData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodolistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvent()
    }

    private fun registerEvent() {
        binding.addBtnHome.setOnClickListener {
            val intent = Intent(requireActivity(), AddTask_Activity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClicked(toDoData: ToDoData, position: Int) {
        Toast.makeText(requireContext(), "Task: ${toDoData.task}, Description: ${toDoData.desc}", Toast.LENGTH_SHORT).show()
    }
//Initializes Firebase_auth, DR, RV,list w adapter.
    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())
        databaseRef1 = FirebaseDatabase.getInstance().reference
            .child("History").child(auth.currentUser?.uid.toString())
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = Add_ToDoList_Adapter(mList)
        adapter.setListener(this)
        binding.recycler.adapter = adapter
    }
//yekho data mn firebase w y3abi el mlist mte3na b  ToDoData objects
    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key ?: ""
                    val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                    val desc = taskSnapshot.child("desc").getValue(String::class.java) ?: ""
                    val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
// el snapshot It helps el app stay in sync with the latest data
                    val imageUri = taskSnapshot.child("imageUri").getValue(String::class.java)
                    val priority = Priority.valueOf(taskSnapshot.child("priority").getValue(String::class.java) ?: Priority.NORMAL.name)
                    val reminderTimeInMillis = taskSnapshot.child("reminderTime").getValue(Long::class.java) ?: -1L
                    val formattedReminderTime = formatReminderTime(reminderTimeInMillis)
                    val todoTask = ToDoData(taskId, task, desc, date, imageUri, priority = priority, reminderTime = reminderTimeInMillis, formattedReminderTime = formattedReminderTime)


                    mList.add(todoTask)
                }
                sortAndDisplayTasksByPriority()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun formatReminderTime(reminderTimeInMillis: Long): String {
        if (reminderTimeInMillis == -1L) return "" // Return empty string if no reminder time is set

        val hours = (reminderTimeInMillis % (60 * 60 * 1000))
        val minutes = (reminderTimeInMillis % (60 * 60 * 1000)) / (60 * 1000)

        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    private fun sortAndDisplayTasksByPriority() {
        adapter.updateList(mList.sortedByDescending { it.priority })
    }

    override fun onDissatisfiedIconClicked(toDoData: ToDoData, position: Int) {
        // Change the icon immediately
        adapter.notifyItemChanged(position)
        // Use Handler to delay the save and delete operations
        Handler(Looper.getMainLooper()).postDelayed({
            // Remove the task from the list
            mList.removeAt(position)
            adapter.notifyItemRemoved(position)

            // Save the task to history
            saveTaskToHistory(toDoData)
            // Delete the task from Firebase
            deleteTaskFromFirebase(toDoData.taskId)

            Toast.makeText(context, "Task Completed successfully", Toast.LENGTH_SHORT).show()
        }, 2000) // Delay of 2 seconds
    }

    private fun saveTaskToHistory(toDoData: ToDoData) {
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
                Toast.makeText(context, "Task added to history successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deleteTaskFromFirebase(taskId: String) {
        databaseRef.child(taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task Completed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    //lehne el putExtra heya eli t3awed traja3 les donnees lel AddTask_Activity page fehom el data mtaa el task eli nzelt aleha bch tediti fou9ha
    override fun onEditItemClicked(toDoData: ToDoData, position: Int, context: Context) {
        val intent = Intent(context, AddTask_Activity::class.java)
        intent.putExtra("taskId", toDoData.taskId)
        intent.putExtra("task", toDoData.task)
        intent.putExtra("desc", toDoData.desc)
        intent.putExtra("date", toDoData.date)
        intent.putExtra("reminderTime",toDoData.reminderTime)
        intent.putExtra("imageUri", toDoData.imageUri) // Pass the imageUri as a string
        intent.putExtra("priority", toDoData.priority)
        intent.putExtra("isEditMode", true)
        startActivity(intent)
    }



}
