package com.example.agtia.todofirst.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Friend(
    var id: String,
    var senderEmail: String,
    val recipientEmail: String,
    val status: String
) : Parcelable {
    companion object {
        const val STATUS_PENDING = "pending"
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "senderEmail" to senderEmail,
            "recipientEmail" to recipientEmail,
            "status" to status
        )
    }
}
