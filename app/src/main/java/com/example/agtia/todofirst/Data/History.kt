package com.example.agtia.todofirst.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History(
    val taskId: String,
    var task: String,
    var desc: String,
    var date: String = "",
    var done: Boolean = true,
    var imageUri: String? = null,
    var priority: Priority = Priority.NORMAL,
    var reminderTime: Long = 0 // Add time field
) : Parcelable {


    fun toMap(): Map<String, Any?> {
        return mapOf(
            "taskId" to taskId,
            "task" to task,
            "desc" to desc,
            "date" to date,
            "imageUri" to imageUri,
            "done" to done,
            "priority" to priority.name, // Store priority as string in database
            "reminderTime" to reminderTime // Add time to map
        )
    }
}
