package com.example.agtia.Admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.Toast
import com.example.agtia.Authentication.Log_In_Activity
import com.example.agtia.R
import com.example.agtia.todofirst.Data.User


class AdminActivity : AppCompatActivity(), MyAdapterAdmin.OnDeleteClickListener {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapterAdmin: MyAdapterAdmin
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private lateinit var btnExitUser: Button
    private lateinit var originalList: List<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        btnExitUser = findViewById(R.id.quitte)
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.search)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()
        myAdapterAdmin = MyAdapterAdmin(userArrayList, this)
        recyclerView.adapter = myAdapterAdmin

        EventChangeListener()
        btnExitUser.setOnClickListener {
            startActivity(Intent(applicationContext, Log_In_Activity::class.java))
            finish()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }


    private fun filter(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            originalList // If query is empty, show originalList
        } else {
            originalList.filter {
                it.email?.contains(query, ignoreCase = true) ?: false
            }
        }
        myAdapterAdmin.filterList(filteredList)
    }


    override fun onDeleteClick(email: String) {
        deleteUserByEmail(email)
        EventChangeListener()

    }

    private fun deleteUserByEmail(emailToDelete: String) {
        val currentUser = auth.currentUser

        // Ensure current user is authenticated
        if (currentUser != null) {
            // Proceed with deletion
            db.collection("users").whereEqualTo("email", emailToDelete).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result!!.isEmpty) {
                        val documentSnapshot = task.result!!.documents[0]
                        val documentId = documentSnapshot.id

                        // Delete the user from Firestore
                        db.collection("users").document(documentId).delete()
                            .addOnSuccessListener {
                                // Successfully deleted user from Firestore
                                Toast.makeText(this, "Successfully Deleted User from Firestore", Toast.LENGTH_SHORT).show()

                                // Now delete the user from Authentication
                                currentUser.delete()
                                    .addOnSuccessListener {
                                        Log.d("AdminActivity", "Successfully Deleted User from Authentication")
                                        Toast.makeText(this, "Successfully Deleted User from Authentication", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { authError ->
                                        Log.e("AdminActivity", "Error Deleting User from Authentication: ${authError.message}")
                                        Toast.makeText(this, "Error Deleting User from Authentication: ${authError.message}", Toast.LENGTH_SHORT).show()
                                    }

                                // Refresh the list after deletion
                            }
                            .addOnFailureListener { firestoreError ->
                                Toast.makeText(this, "Error Deleting User from Firestore: ${firestoreError.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "User with specified email not found", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Handle case when current user is not authenticated
            Toast.makeText(this, "Current user is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun EventChangeListener() {
        db.collection("users").addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("FireStoreError", error.message.toString())
                return@addSnapshotListener
            }

            value?.let { snapshot ->
                val updatedList = ArrayList<User>()

                for (dc: DocumentChange in snapshot.documentChanges) {
                    val user = dc.document.toObject(User::class.java)

                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            updatedList.add(user)
                        }
                        DocumentChange.Type.REMOVED -> {
                            updatedList.removeAll { it.email == user.email }
                        }
                        DocumentChange.Type.MODIFIED -> {

                        }
                    }
                }


                originalList = updatedList.toList()
                userArrayList.clear()
                userArrayList.addAll(updatedList)
                myAdapterAdmin.filterList(updatedList)
            }
        }
    }


}
