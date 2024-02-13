package com.example.agtia

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agtia.databinding.FragmentHistoriqueOfListsBinding
import com.example.agtia.todofirst.utils.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class historique_of_lists : Fragment(),
    HistoriqueAdapter.OnDeleteClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var binding: FragmentHistoriqueOfListsBinding

    private lateinit var adapter:HistoriqueAdapter
    private lateinit var mList: MutableList<History>

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

        binding.quitte.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }


    }


    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()

        databaseRef = FirebaseDatabase.getInstance().reference
            .child("History").child(auth.currentUser?.uid.toString())
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = HistoriqueAdapter(mList)
        adapter.setOnDeleteClickListener(this)
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

                    // Create a single ToDoData object with task name and description
                    val todoTask = History(taskId, task, desc,date)

                    // Add the ToDoData object to the list
                    mList.add(todoTask)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
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

