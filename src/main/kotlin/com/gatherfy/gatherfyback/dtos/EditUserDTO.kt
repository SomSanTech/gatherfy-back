package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class EditUserDTO(
    var firstname: String,
    var lastname: String,
    var username: String,
    var gender: String,
    var email: String,
    var phone: String,
    var image: String,
    var birthday: LocalDateTime,
    var password: String
)
