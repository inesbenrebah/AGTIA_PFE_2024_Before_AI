package com.example.agtia

data class History(
    val taskId: String,
    var task: String,
    var desc: String,
    var date: String = "",
    var done: Boolean = true
)
