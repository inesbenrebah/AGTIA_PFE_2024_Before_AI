package com.example.agtia.todofirst.utils

import android.os.Parcelable
import com.example.agtia.Priority // Import the Priority enum from the correct package

import kotlinx.parcelize.Parcelize

@Parcelize
data class ToDoData(
    var taskId: String = "",
    var task: String = "",
    var desc: String = "",
    var date: String = "",
    var imageUri: String? = null,
    var done: Boolean = false,
    var priority: Priority = Priority.NORMAL // Default to normal priority
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "taskId" to taskId,
            "task" to task,
            "desc" to desc,
            "date" to date,
            "imageUri" to imageUri,
            "done" to done,
            "priority" to priority.name // Store priority as string in database
        )
    }
}
