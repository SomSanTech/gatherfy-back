package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class CreateUserDTO(
    var firstname: String,
    var lastname: String,
    var username: String,
    var gender: String,
    var email: String,
    var phone: String,
//    var image: String,
    var role: String,
    var birthday: LocalDateTime,
    var password: String
)
