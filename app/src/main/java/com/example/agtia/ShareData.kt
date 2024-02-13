package com.example.agtia

data class ShareData(
    var sendTo : String ="",
    var taskId: String = "",
    var task: String = "",
    var desc: String = "",
    var date: String = "",
    var priority: Boolean =false,
    var done: Boolean = false
)
