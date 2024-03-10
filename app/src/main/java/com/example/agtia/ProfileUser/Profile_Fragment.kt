package com.example.agtia.ProfileUser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.Authentication.Log_In_Activity
import com.example.agtia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class Profile_Fragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userJob: TextView
    private lateinit var editProfileButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var friendsListButton: Button
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: Profile_Adapter

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val usersCollection = FirebaseFirestore.getInstance().collection("users")
    private val usersDatabaseRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mine, container, false)

        profileImage = view.findViewById(R.id.profile_image)
        userName = view.findViewById(R.id.user_name)
        userEmail = view.findViewById(R.id.user_email)
        userJob = view.findViewById(R.id.user_job)
        friendsListButton=view.findViewById(R.id.MyFriends)
        editProfileButton = view.findViewById(R.id.edit_profile_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)


        // Retrieve user data from Firestore
        currentUser?.uid?.let { uid ->
            usersCollection.document(uid).get().addOnSuccessListener { documentSnapshot ->
                val userData = documentSnapshot.data
                if (userData != null) {
                    userName.text = "${userData["firstName"]} ${userData["lastName"]}"
                    userEmail.text = userData["email"].toString()
                    userJob.text = userData["job"].toString()

                    val photoUrl = userData["photoUrl"].toString()
                    if (photoUrl.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(photoUrl)
                            .placeholder(R.drawable.baseline_sentiment_very_dissatisfied_24) // Placeholder image while loading
                            .error(R.drawable.baseline_blur_circular_24) // Error image if loading fails
                            .circleCrop()
                            .into(profileImage)
                    }
                }
            }
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), Edit_Profile_Activity::class.java)
            startActivity(intent)
        }
        friendsListButton.setOnClickListener {


            val intent = Intent(requireContext(), My_Friends_List_Activity::class.java)

            startActivity(intent)
        }

        deleteAccountButton.setOnClickListener {
            val intent = Intent(requireActivity(), Confirmation_Activity::class.java)
            startActivity(intent)
        }




        return view
    }
}
