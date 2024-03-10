package com.example.agtia.Share_Task.Main_Share

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.agtia.R
import com.example.agtia.databinding.ActivityAddSharingTaskBinding
import com.example.agtia.todofirst.Data.Priority
import com.example.agtia.todofirst.Data.ShareData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale
import java.util.Random

class Add_Sharing_Task : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityAddSharingTaskBinding
    private lateinit var prioritySpinner: Spinner
    private var selectedReminderTimeInMillis: Long = -1
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
        binding = ActivityAddSharingTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        setupViews()

        // Retrieve extras
        val taskId = intent.getStringExtra("taskId")
        val task = intent.getStringExtra("task")
        val desc = intent.getStringExtra("desc")
        val date = intent.getStringExtra("date")
        val imageUri = intent.getStringExtra("imageUri") ?: "" // retrieve imageUri
        Log.d("AddActivity", "Received image URI: $imageUri")
        val priorityString = intent.getStringExtra("priority")
        val priority = Priority.valueOf(priorityString ?: Priority.NORMAL.name) // Default to NORMAL if priorityString is null
        val reminderTime = intent.getStringExtra("reminderTime")
        val isEditMode = intent.getBooleanExtra("isEditMode", false)


        prioritySpinner = findViewById(R.id.prioritySpinner)
        val priorityLevels = arrayOf("Low", "Normal", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter

        if (isEditMode) {
            binding.todoEt.setText(task)
            binding.todoDesc.setText(desc)
            binding.selectedDateTextView.text = date
            val priorityIndex = when (priority) {
                Priority.LOW -> 0
                Priority.NORMAL -> 1
                Priority.HIGH -> 2
            }
            prioritySpinner.setSelection(priorityIndex)

            if (imageUri.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(imageUri))
                    .into(binding.imageView)
                binding.imageView.tag = Uri.parse(imageUri)
                binding.imageView.visibility = View.VISIBLE
            }

        }
    }

    private fun setupViews() {
        binding.todoClose.setOnClickListener {
            finish()
        }

        binding.selectDateButton.setOnClickListener {
            showDatePicker()
            binding.selectDateButton.setImageResource(R.drawable.baseline_calendar_month_24)
        }

        binding.addImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.todoNextBtn.setOnClickListener {
            onSaveTask()
        }
        binding.alarm.setOnClickListener {
            showTimePicker()
            binding.alarm.setImageResource(R.drawable.baseline_alarm_on_24)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate =
                    String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                binding.selectedDateTextView.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // malezmch yhot date kbal lyouma
        datePicker.datePicker.minDate = calendar.timeInMillis

        datePicker.show()
    }



    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedCalendar.set(Calendar.MINUTE, selectedMinute)
                selectedCalendar.set(Calendar.SECOND, 0)
                selectedReminderTimeInMillis = selectedCalendar.timeInMillis

                val hours = selectedHour
                val minutes = selectedMinute

                val selectedTime =
                    String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
                binding.selectedReminderTextView.text = selectedTime
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    fun encodeEmail(email: String): String {
        return email.replace(".", "-")
    }

    private fun onSaveTask() {
        val currentUserEmail = auth.currentUser?.email
        val user2uid=auth.currentUser?.uid.toString()
        Log.d("amine ","${currentUserEmail}")
        val todoEmail = binding.todoEmail.text.toString()
        val todo = binding.todoEt.text.toString()
        val todoDesc = binding.todoDesc.text.toString()
        val date = binding.selectedDateTextView.text.toString()

        // Retrieve the selected reminder time
        val reminderTime = selectedReminderTimeInMillis

        val priority = when (prioritySpinner.selectedItemPosition) {
            0 -> Priority.LOW
            1 -> Priority.NORMAL
            2 -> Priority.HIGH
            else -> Priority.NORMAL
        }

        if (todo.isNotEmpty() && todoDesc.isNotEmpty() && currentUserEmail != null && todoEmail.isNotEmpty()) {
            // Check if todoEmail is in the current user's friend list
            val friendListRef = FirebaseDatabase.getInstance().reference
                .child("MyFriendsList")
                .child(encodeEmail(currentUserEmail))
            friendListRef.child(encodeEmail(todoEmail)).get().addOnCompleteListener { friendCheckTask ->
                if (friendCheckTask.isSuccessful) {
                    val friendExists = friendCheckTask.result?.value != null
                    if (friendExists) {
                        Log.d("ekhermara","hedhi prioerity ${priority}")
                        // todoEmail is in the current user's friend list
                        Log.d("ekhermara","From  :${encodeEmail(currentUserEmail)}")
                        Log.d("ekhermara","To  :${todoEmail}")
                        val newTaskData = ShareData(
                            task = todo,
                            emailFrom = encodeEmail(currentUserEmail),
                            user2uid=user2uid,
                            emailTo = todoEmail,
                            desc = todoDesc,
                            date = date,
                            reminderTime = reminderTime,
                            priority = priority
                        )
                        newTaskData.imageUri = getImageUri()?.toString()
                        val taskId = intent.getStringExtra("taskId")
                        if (taskId != null) {
                            // Update existing task
                            newTaskData.taskId = taskId
                            // Schedule reminder if a reminder time is set

                            updateTaskInDatabase(newTaskData) // Update the task directly
                        } else {
                            // Save new task
                            saveNewTaskToDatabase(newTaskData)
                        }
                    } else {
                        // todoEmail is not in the current user's friend list
                        Toast.makeText(this, "The user must be in your friend list", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to check friend's friend list: ${friendCheckTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNewTaskToDatabase(taskData: ShareData) {
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("ShareData")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        // Save user image to Firebase Storage and retrieve profile photo URL from Firestore
        currentUser?.uid?.let { uid ->
            usersCollection.document(uid).get().addOnSuccessListener { documentSnapshot ->
                val userData = documentSnapshot.data
                if (userData != null) {
                    // Retrieve user profile photo URL from Firestore
                    val photoUrl = userData["photoUrl"].toString()

                    // Assign user profile photo URL to the task data
                    taskData.userPhotoUrl = photoUrl

                    // Save task data to the Firebase Realtime Database
                    val newTaskRef = databaseRef.push()
                    val newTaskId = newTaskRef.key ?: ""
                    taskData.taskId = newTaskId

                    val taskMap = taskData.toMap()

                    newTaskRef.setValue(taskMap).addOnCompleteListener { databaseTask ->
                        if (databaseTask.isSuccessful) {
                            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to AddTask_Activity task: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateTaskInDatabase(taskData: ShareData) {
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("ShareData").child(auth.currentUser?.uid.toString())
        val taskId = taskData.taskId
        val taskMap = taskData.toMap()
        databaseRef.child(taskId).updateChildren(taskMap)
            .addOnCompleteListener { databaseTask ->
                if (databaseTask.isSuccessful) {
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update task: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getImageUri(): Uri? {
        return binding.imageView.tag as? Uri
    }
}


