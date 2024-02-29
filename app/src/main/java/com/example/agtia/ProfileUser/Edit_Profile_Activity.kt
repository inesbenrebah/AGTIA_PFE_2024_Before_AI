package com.example.agtia.ProfileUser

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.agtia.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Edit_Profile_Activity : AppCompatActivity() {

    private lateinit var editTextFirstName: TextInputEditText
    private lateinit var editTextLastName: TextInputEditText
    private lateinit var editTextJob: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var selectPhotoButton: Button
    private lateinit var profileImage: ImageView
    private var selectedPhotoUri: Uri? = null

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val usersCollection = FirebaseFirestore.getInstance().collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editTextFirstName = findViewById(R.id.edit_text_first_name)
        editTextLastName = findViewById(R.id.edit_text_last_name)
        editTextJob = findViewById(R.id.edit_text_job)
        saveButton = findViewById(R.id.save_changes_button)
        selectPhotoButton = findViewById(R.id.select_photo_button)
        profileImage = findViewById(R.id.profile_image)

        currentUser?.uid?.let { uid ->
            usersCollection.document(uid).get().addOnSuccessListener { documentSnapshot ->
                val userData = documentSnapshot.data
                if (userData != null) {
                    editTextFirstName.setText(userData["firstName"].toString())
                    editTextLastName.setText(userData["lastName"].toString())
                    editTextJob.setText(userData["job"].toString())
                    // Load profile image if available
                    val photoUrl = userData["photoUrl"].toString()
                    if (photoUrl.isNotEmpty()) {

                    }
                }
            }
        }

        selectPhotoButton.setOnClickListener {
            // Open the photo selection dialog
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        saveButton.setOnClickListener {
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()
            val job = editTextJob.text.toString()

            currentUser?.uid?.let { uid ->
                val updatedData = hashMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "job" to job
                )

                usersCollection.document(uid).update(updatedData as Map<String, Any>)
                    .addOnSuccessListener {
                        if (selectedPhotoUri != null) {
                            uploadPhotoToStorage(uid)
                        } else {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun uploadPhotoToStorage(userId: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val photoRef = storageReference.child("profile_photos/$userId")

        photoRef.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    val photoUrl = uri.toString()
                    usersCollection.document(userId).update("photoUrl", photoUrl)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
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
                Toast.makeText(this, "Failed to upload photo: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            selectedPhotoUri = data?.data
            profileImage.setImageURI(selectedPhotoUri)
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
}
