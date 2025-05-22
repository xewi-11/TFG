package com.example.bookcloud.model

import com.google.firebase.Timestamp

data class Message(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp? = null
)
