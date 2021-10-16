package com.example.firebasechat.model

//Firebase veritabanıyla buradaki verilerin eşleşebilmesi için initialize ederken boş tanımlıyoruz.
data class User(
    var name: String = "",
    var profile_image: String = "",
    var status: String = ""
)
