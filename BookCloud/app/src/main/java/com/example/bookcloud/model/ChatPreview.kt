package com.example.bookcloud.model

class ChatPreview(
                  val chatId: String,
                  val withUserId: String,
                  val bookId: String,
                  var userName: String = "",
                  var userPhotoUrl: String? = null,
                  var bookTitle: String = "") {
}