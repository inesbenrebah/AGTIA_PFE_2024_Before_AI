package com.example.agtia.Authentication
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.agtia.R
import com.example.agtia.todofirst.Data.AllEmails
import com.example.agtia.todofirst.Data.Friend
import com.example.agtia.todofirst.Data.Priority
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.agtia.todofirst.Data.job.Tester
import com.example.agtia.todofirst.Data.job.Developer
import com.example.agtia.todofirst.Data.job.Designer
import com.google.firebase.database.FirebaseDatabase

class Sign_Up_Activity : AppCompatActivity() {
    private lateinit var editTextFirstName: TextInputEditText
    private lateinit var editTextLastName: TextInputEditText
    private lateinit var editTextJob: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextRePassword: TextInputEditText
    private lateinit var signin: Button
    private lateinit var jobSpinner: Spinner
    private lateinit var signup: TextView
    private lateinit var selectPhotoButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    private var selectedPhotoUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->


            uri?.let {
                selectedPhotoUri = it
                findViewById<ImageView>(R.id.photo).setImageURI(it)
                selectPhotoButton.text = "Photo Selected"
                // Remove circular shape background
                findViewById<ImageView>(R.id.photo).background = null
            }


        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        firebaseAuth = FirebaseAuth.getInstance()

        editTextFirstName = findViewById(R.id.first_name)
        editTextLastName = findViewById(R.id.last_name)

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextRePassword = findViewById(R.id.repassword)
        signin = findViewById(R.id.signup)
        signup = findViewById(R.id.sign_up)
        selectPhotoButton = findViewById(R.id.select_photo_button)

        signup.setOnClickListener {
            val intent = Intent(this, Log_In_Activity::class.java)
            startActivity(intent)
        }
        jobSpinner = findViewById(R.id.jobSpinner)

        val jobLevels = arrayOf("Developer", "Designer", "Tester")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jobLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jobSpinner.adapter = adapter

        selectPhotoButton.setOnClickListener {
            // Open the photo selection dialog
            getContent.launch("image/*")
        }

        signin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val repassword = editTextRePassword.text.toString()
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && repassword.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && selectedPhotoUri != null) {
                if (password == repassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val currentUser = FirebaseAuth.getInstance().currentUser

                                currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Verification email sent. Please verify your email address.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val intent = Intent(this, Log_In_Activity::class.java)

                                            val job = when (jobSpinner.selectedItemPosition) {
                                                0 -> Developer
                                                1 -> Designer
                                                2 -> Tester
                                                else -> Developer // Default value if position is not recognized
                                            }
                                            SaveEmail(email,firstName)
                                            val usersCollection =
                                                FirebaseFirestore.getInstance().collection("users")
                                            val user = hashMapOf(
                                                "email" to email,
                                                "firstName" to firstName,
                                                "lastName" to lastName,
                                                "job" to job
                                            )

                                            usersCollection.document(currentUser.uid).set(user)
                                                .addOnSuccessListener {
                                                    uploadPhotoToStorage(currentUser.uid)
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(
                                                        this,
                                                        "Failed to save user data: ${exception.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Failed to send verification email.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to create user: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "The password and the confirm password don't match.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Fill in all the fields and select a photo.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun uploadPhotoToStorage(userId: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val photoRef = storageReference.child("profile_photos/$userId")

        photoRef.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    val photoUrl = uri.toString()
                    val usersCollection = FirebaseFirestore.getInstance().collection("users")
                    usersCollection.document(userId).update("photoUrl", photoUrl)
                        .addOnSuccessListener {
                            val intent = Intent(this, Log_In_Activity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Failed to save photo URL: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to upload photo: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun SaveEmail(email: String, firstname:String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("AllEmails").child(encodeEmail(email))

        val requestFriend = AllEmails(email,firstname)
        val requestMap = requestFriend.toMap()

        databaseRef.setValue(requestMap).addOnCompleteListener { databaseTask ->
            if (databaseTask.isSuccessful) {
                Toast.makeText(this, "Email saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Failed to save email: ${databaseTask.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }

}
