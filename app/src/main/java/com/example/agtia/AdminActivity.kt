package com.example.agtia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.Toast


class AdminActivity : AppCompatActivity(), MyAdapterAdmin.OnDeleteClickListener {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapterAdmin: MyAdapterAdmin
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private lateinit var btnExitUser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        btnExitUser = findViewById(R.id.quitte)
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.search) // Add this line to initialize the searchView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()
        myAdapterAdmin = MyAdapterAdmin(userArrayList, this)
        recyclerView.adapter = myAdapterAdmin

        EventChangeListener()
        btnExitUser.setOnClickListener {
            startActivity(Intent(applicationContext,MainActivity::class.java))
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
        val filteredList = ArrayList<User>()
        userArrayList.forEach { user ->
            if (user.email?.contains(query.orEmpty(), true) == true) {
                filteredList.add(user)
            }
        }
        myAdapterAdmin.filterList(filteredList)
    }

    override fun onDeleteClick(email: String) {
        deleteUserByEmail(email)
    }

    private fun deleteUserByEmail(emailToDelete: String) {
        db.collection("users").whereEqualTo("email", emailToDelete).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result!!.isEmpty) {
                    val documentSnapshot = task.result!!.documents[0]
                    val documentId = documentSnapshot.id

                    // Delete the user from Firestore
                    db.collection("users").document(documentId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successfully Deleted User from Firestore", Toast.LENGTH_SHORT).show()
                            // Refresh the list after deletion
                            EventChangeListener()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error Deleting User from Firestore", Toast.LENGTH_SHORT).show()
                        }


                    val user = auth.currentUser
                    user?.delete()

                        ?.addOnSuccessListener {
                            Log.d("AdminActivity", "Successfully Deleted User from Authentication")
                            Toast.makeText(this, "Successfully Deleted User from Authentication", Toast.LENGTH_SHORT).show()

                        }
                        ?.addOnFailureListener {
                            Log.e("AdminActivity", "Error Deleting User from Authentication: ${it.message}")
                            Toast.makeText(this, "Error Deleting User from Authentication", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "User with specified email not found", Toast.LENGTH_SHORT).show()

                }
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
                            // Handle modified user if needed
                        }
                    }
                }

                myAdapterAdmin.filterList(updatedList)
            }
        }
    }



}
