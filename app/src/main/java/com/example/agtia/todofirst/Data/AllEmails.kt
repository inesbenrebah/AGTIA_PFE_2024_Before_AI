package com.example.agtia.todofirst.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AllEmails(
    var email: String ="",
    var firstname:String =""
): Parcelable {
fun toMap(): Map<String, Any?> {
    return mapOf(
        "email" to email)
"firstname" to firstname}}
