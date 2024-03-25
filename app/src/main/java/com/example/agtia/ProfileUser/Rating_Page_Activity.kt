// File: Rating_Page_Activity.kt
package com.example.agtia.ProfileUser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.databinding.ActivityRatingPageBinding
import com.example.agtia.todofirst.Data.RatingStars
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Rating_Page_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityRatingPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var ratingPage: Adapter_Rating_Page
    private var List: ArrayList<RatingStars> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRatingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("RatingStars")
        recyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        ratingPage = Adapter_Rating_Page(List)
        recyclerView.adapter = ratingPage
        getDataFromFirebase()
    }
    private fun getDataFromFirebase() {
        // Retrieve rating stars data
        databaseRef.orderByChild("stars")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    List.clear()
                    if (snapshot.exists()) {
                        val sortedList = snapshot.children.map { rateSnapshot ->
                            val email = rateSnapshot.child("email").getValue(String::class.java) ?: ""
                            val stars = rateSnapshot.child("stars").getValue(Float::class.java) ?: 0.0F
                            RatingStars(email = email, stars = stars)
                        }.sortedByDescending { it.stars }

                        // Add sorted list to the main list
                        List.addAll(sortedList)

                        ratingPage.notifyDataSetChanged()
                    } else {
                        // User doesn't have any friends, display a message or show a placeholder UI
                        Log.d("getDataFromFirebase", "No friends found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("getDataFromFirebase", "Database Error: ${error.message}")
                }
            })
    }

    private fun encodeEmail(email: String): String {
        // Encode email by replacing dots with dashes
        return email.replace(".", "-")
    }


}
