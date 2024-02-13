package com.example.agtia

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.databinding.FragmentTodolistBinding
import com.example.agtia.todofirst.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Todolist : Fragment(), AddTodoPopUpFragment.DialogNextBtnClickListener, TodoAdapter.ToDoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef1: DatabaseReference
    private lateinit var binding: FragmentTodolistBinding

    private lateinit var adapter: TodoAdapter
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
            val intent = Intent(requireActivity(), add::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClicked(toDoData: ToDoData, position: Int) {
        Toast.makeText(requireContext(), "Task: ${toDoData.task}, Description: ${toDoData.desc}", Toast.LENGTH_SHORT).show()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())
        databaseRef1 = FirebaseDatabase.getInstance().reference
            .child("History").child(auth.currentUser?.uid.toString())
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = TodoAdapter(mList)
        adapter.setListener(this)
        binding.recycler.adapter = adapter
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key ?: ""
                    val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                    val desc = taskSnapshot.child("desc").getValue(String::class.java) ?: ""
                    val date = taskSnapshot.child("date").getValue(String::class.java) ?: ""
                    val imageUri = taskSnapshot.child("imageUri").getValue(String::class.java)
                    val priority = Priority.valueOf(taskSnapshot.child("priority").getValue(String::class.java) ?: Priority.NORMAL.name)
                    val todoTask = ToDoData(taskId, task, desc, date, imageUri, priority=priority)
                    mList.add(todoTask)
                }
                sortAndDisplayTasksByPriority()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sortAndDisplayTasksByPriority() {
        adapter.updateList(mList.sortedByDescending { it.priority })
    }

    override fun onDissatisfiedIconClicked(toDoData: ToDoData, position: Int) {
        mList.removeAt(position)
        adapter.notifyItemRemoved(position)
        toDoData.done = true
        deleteTaskFromFirebase(toDoData.taskId)
        saveTaskToHistory(toDoData)
        adapter.notifyDataSetChanged()
    }

    private fun saveTaskToHistory(toDoData: ToDoData) {
        val newTaskRef = databaseRef1.push()
        val taskId = newTaskRef.key ?: ""
        val newTaskHistory = History(taskId, toDoData.task, toDoData.desc,toDoData.date)
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
    override fun onSaveTask(todo: String, todoDesc: TextInputEditText, todoEt: TextInputEditText, date: String) {
        // Not needed in Todolist fragment
    }

    override fun onUpdateTask(toDoData: ToDoData, todoDesc: TextInputEditText, todoEt: TextInputEditText) {
        // Not needed in Todolist fragment
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

    override fun onEditItemClicked(toDoData: ToDoData, position: Int, context: Context) {
        val intent = Intent(context, add::class.java)
        intent.putExtra("ToDoData", toDoData)
        intent.putExtra("isEditMode", true) // Indicate that it's in edit mode
        intent.putExtra("taskId", toDoData.taskId) // Pass the task ID
        context.startActivity(intent)
    }

}
