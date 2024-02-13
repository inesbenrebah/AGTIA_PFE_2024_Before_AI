package com.example.agtia

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.agtia.databinding.ActivityAddBinding
import com.example.agtia.todofirst.utils.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class add : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAddBinding
    private lateinit var prioritySpinner: Spinner

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageView.setImageURI(it)
                binding.imageView.tag = it
                binding.imageView.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupViews()

        prioritySpinner = findViewById(R.id.prioritySpinner)
        val priorityLevels = arrayOf("Low", "Normal", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter
    }

    private fun setupViews() {
        binding.todoClose.setOnClickListener {
            finish()
        }

        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.addImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.todoNextBtn.setOnClickListener {
            onSaveTask()
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate =
                    String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                binding.selectedDateTextView.text = selectedDate
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun onSaveTask() {
        val todo = binding.todoEt.text.toString()
        val todoDesc = binding.todoDesc.text.toString()
        val date = binding.selectedDateTextView.text.toString()
        val priority = when (prioritySpinner.selectedItemPosition) {
            0 -> Priority.LOW
            1 -> Priority.NORMAL
            2 -> Priority.HIGH
            else -> Priority.NORMAL // Default to normal if unexpected position
        }

        if (todo.isNotEmpty() && todoDesc.isNotEmpty()) {
            val newTaskData = ToDoData(task = todo, desc = todoDesc, date = date, priority = priority)
            val taskId = intent.getStringExtra("taskId") // Retrieve the task ID
            saveTaskToDatabase(newTaskData, taskId)
        } else {
            Toast.makeText(this, "Please fill in both task and description", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTaskToDatabase(taskData: ToDoData, taskId: String?) {
        // Get reference to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        // Create a reference to the location where you want to store the image
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        // Get the image URI
        val imageUri = getImageUri()

        // Upload the image to Firebase Storage
        imageUri?.let { uri ->
            val uploadTask = imagesRef.putFile(uri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                // Continue with the task to get the download URL
                imagesRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Image uploaded successfully, get the download URL
                    val imageUrl = task.result.toString()
                    // Set the image URL to task data
                    taskData.imageUri = imageUrl

                    // Get reference to Firebase Realtime Database
                    val databaseRef = FirebaseDatabase.getInstance().reference
                        .child("Tasks").child(auth.currentUser?.uid.toString())

                    // If taskId is null, it means we are creating a new task
                    if (taskId == null) {
                        // Push the task data to the database
                        val newTaskRef = databaseRef.push()
                        val newTaskId = newTaskRef.key ?: ""
                        taskData.taskId = newTaskId

                        // Convert taskData to a map
                        val taskMap = taskData.toMap()

                        newTaskRef.setValue(taskMap).addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to add task: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Otherwise, we are updating an existing task
                        val taskRef = databaseRef.child(taskId)
                        val taskMap = taskData.toMap()

                        taskRef.updateChildren(taskMap).addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to update task: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Handle failures
                    Toast.makeText(this, "Failed to upload image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            // No image selected
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUri(): Uri? {
        return binding.imageView.tag as? Uri
    }
}
