package com.example.agtia.History
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.databinding.FragmentHistoriqueOfListsBinding
import com.example.agtia.todofirst.Data.History
import com.example.agtia.todofirst.Data.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class History_Of_Tasks_Fragment : Fragment(), History_Adapter.OnDeleteClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef1: DatabaseReference
    private lateinit var binding: FragmentHistoriqueOfListsBinding

    private lateinit var adapter: History_Adapter
    private lateinit var mList: MutableList<History>
    private lateinit var originalList: List<History>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoriqueOfListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()


    }



    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        originalList = emptyList()
        databaseRef1 = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("History").child(auth.currentUser?.uid.toString())
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()

        adapter = History_Adapter(requireContext(), mList)
        adapter.setOnDeleteClickListener(this)
        binding.recycler.adapter = adapter
    }

    private fun saveTaskToHistory(history: History) {
        val newTaskRef = databaseRef1.push()
        val taskId = newTaskRef.key ?: ""
        val newTaskHistory = History(
            taskId,
            history.task,
            history.desc,
            history.date,
            history.done,
            history.imageUri,
            history.priority,
            history.reminderTime
        )
        newTaskRef.setValue(newTaskHistory).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task added to history successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onReturnTaskClick(history: History, position: Int) {
        mList.removeAt(position)
        adapter.notifyItemRemoved(position)
        history.done = false
        deleteTaskFromFirebase(history.taskId)
        saveTaskToHistory(history)
        adapter.notifyDataSetChanged()
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
                    val priorityStr = taskSnapshot.child("priority").getValue(String::class.java)
                    val priority = Priority.valueOf(priorityStr ?: Priority.NORMAL.name)
                    val reminderTime = taskSnapshot.child("reminderTime").getValue(Long::class.java) ?: 0L
                    val todoTask = History(taskId, task, desc, date, imageUri = imageUri, priority = priority, reminderTime = reminderTime)


                    mList.add(todoTask)
                }
                sortAndDisplayTasksByDate()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sortAndDisplayTasksByDate() {
        adapter.updateList(mList.sortedBy { it.date })
    }

    override fun onDeleteClick(history: History, position: Int) {
        databaseRef.child(history.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
