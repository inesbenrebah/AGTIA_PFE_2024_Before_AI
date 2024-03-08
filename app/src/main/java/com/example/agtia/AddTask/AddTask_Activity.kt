package com.example.agtia.AddTask
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.agtia.R
import com.example.agtia.databinding.ActivityAddBinding
import com.example.agtia.todofirst.Data.Priority
import com.example.agtia.todofirst.Data.ToDoData
import com.example.agtia.AddTask.ReminderBroadcastReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddTask_Activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityAddBinding
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
        binding = ActivityAddBinding.inflate(layoutInflater)
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
        val reminderTime=intent.getStringExtra("reminderTime")
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


    private fun scheduleNotification(reminderTimeInMillis: Long, taskTitle: String) {
        if (reminderTimeInMillis > 0) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, ReminderBroadcastReceiver::class.java)
            intent.putExtra("notificationId", 101) // Change the notification ID as per your requirement
            intent.putExtra("title", "Your notification title") // Set your notification title here
            intent.putExtra("message", "Your notification message") // Set your notification message here

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent)
        }
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

                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
                binding.selectedReminderTextView.text = selectedTime
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }



    private fun onSaveTask() {
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

        if (todo.isNotEmpty() && todoDesc.isNotEmpty()) {
            val newTaskData = ToDoData(
                task = todo,
                desc = todoDesc,
                date = date,
                reminderTime=reminderTime,
                priority = priority

            )
            newTaskData.imageUri = getImageUri()?.toString()
            val taskId = intent.getStringExtra("taskId")
            if (taskId != null) {
                // Update existing task
                newTaskData.taskId = taskId
                updateTaskInDatabase(newTaskData)
                // Schedule reminder if a reminder time is set
                if (reminderTime > 0) {
                    scheduleNotification(reminderTime, todo)
                }
            } else {
                // Save new task
                saveTaskToDatabase(newTaskData, taskId)
            }
        } else {
            Toast.makeText(this, "Please fill in both task and description", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateTaskInDatabase(taskData: ToDoData) {
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())
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

    private fun saveTaskToDatabase(taskData: ToDoData, taskId: String?) {
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())

        if (taskId == null) {
            // Save new task
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
        } else {
            // Update existing task
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

        // Save image to Firebase Storage
        val imageUri = getImageUri()
        imageUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
            val uploadTask = imagesRef.putFile(uri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    taskData.imageUri = imageUrl

                    // Update task with image URI
                    if (taskId != null) {
                        val taskImageRef = databaseRef.child(taskId).child("imageUri")
                        taskImageRef.setValue(imageUrl)
                    }
                } else {
                    Toast.makeText(this, "Failed to upload image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getImageUri(): Uri? {
        return binding.imageView.tag as? Uri
    }
}
