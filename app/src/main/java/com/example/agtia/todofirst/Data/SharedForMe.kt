package com.example.agtia.todofirst.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedForMe(var emailFrom: String = "",
                       var emailTo: String = "",
                       var taskId: String = "",
                       var task: String = "",
                       var desc: String = "",
                       var date: String = "",
                       var userPhotoUrl: String? = null,
                       var approve:Boolean=false,
                       var imageUri: String? = null,
                       var done: Boolean = false,
                       var priority: Priority = Priority.NORMAL,
                       var reminderTime: Long = -1, // in milliseconds
                       var formattedReminderTime: String = "", // formatted reminder time
                       var iconResource: Int = -1 // resource ID of the associated icon, -1 means no icon
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "emailFrom" to emailFrom,
            "emailTo" to emailTo,
            "taskId" to taskId,
            "task" to task,
            "desc" to desc,
            "date" to date,
            "userPhotoUrl" to userPhotoUrl,
            "approve" to approve,
            "imageUri" to imageUri,
            "done" to done,
            "priority" to priority.name, // Store priority as string in the database
            "reminderTime" to reminderTime, // Store reminder time
            "iconResource" to iconResource // Store icon resource ID
        )
    }
}

