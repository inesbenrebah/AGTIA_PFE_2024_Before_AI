package com.example.agtia.todofirst.Data

data class RequestDelete(
    var emailFrom: String = "",
    var emailTo: String = "",
    var approve: Boolean=false,
    var task: String = "",
    var date: String = ""
)
