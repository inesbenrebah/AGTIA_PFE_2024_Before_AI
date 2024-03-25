package com.example.agtia.todofirst.Data

data class GotDeleted(
    var emailFrom: String = "",
    var emailTo: String = "",
    var taskId: String = "",
    var task: String = "",
    var date: String = "",
    var approve: Boolean=false,
    var rating: Float=00.00F

)
