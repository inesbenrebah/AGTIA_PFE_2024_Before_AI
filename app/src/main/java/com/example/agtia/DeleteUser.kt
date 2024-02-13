package com.example.agtia
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteUser : AppCompatActivity() {
    private lateinit var btnDeleteUser: Button
    private lateinit var email: TextInputEditText
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_user)

        btnDeleteUser = findViewById(R.id.btndelete)
        email = findViewById(R.id.email)

        btnDeleteUser.setOnClickListener {
            val emailToDelete = email.text.toString()



                deleteUserByEmail(emailToDelete)

        }
    }


    private fun deleteUserByEmail(emailToDelete: String) {

        db.collection("users").whereEqualTo("email", emailToDelete).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result!!.isEmpty) {
                    val documentSnapshot = task.result!!.documents[0]
                    val documentId = documentSnapshot.id

                    // Delete the user mn Firestore
                    db.collection("users").document(documentId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this@DeleteUser, "Successfully Deleted User from Firestore", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@DeleteUser, "Error Deleting User from Firestore", Toast.LENGTH_SHORT).show()
                        }


                    val user = auth.currentUser
                    user?.delete()
                        ?.addOnSuccessListener {
                            Toast.makeText(this@DeleteUser, "Successfully Deleted User from Authentication", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this@DeleteUser, "Error Deleting User from Authentication", Toast.LENGTH_SHORT).show()
                        }


                    // intention lel index
                    val intent = Intent(this@DeleteUser, AdminActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@DeleteUser, "User with specified email not found", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
