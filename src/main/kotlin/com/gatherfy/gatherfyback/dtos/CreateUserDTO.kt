package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateUserDTO(
    @field:NotNull(message = "Firstname is required")
    @field:NotBlank(message = "Firstname must not be blank")
    @field:Size(max = 50)
    var firstname: String?,
    @field:NotNull(message = "Lastname is required")
    @field:NotBlank(message = "Lastname must not be blank")
    @field:Size(max = 50)
    var lastname: String?,
    @field:NotNull(message = "Username is required")
    @field:NotBlank(message = "Username must not be blank")
    @field:Size(max = 30)
    var username: String?,
    @field:NotNull(message = "Gender is required")
    @field:NotBlank(message = "Gender must not be blank")
    var gender: String?,
    @field:NotNull(message = "Email is required")
    @field:NotBlank(message = "Email must not be blank")
    @field:Size(max = 100)
    var email: String?,
    @field:NotNull(message = "Phone is required")
    @field:NotBlank(message = "Phone must not be blank")
    @field:Size(max = 10)
    var phone: String?,
//    var image: String,
    @field:NotNull(message = "Role is required")
    @field:NotBlank(message = "Role must not be blank")
    var role: String?,
    var birthday: LocalDateTime?,
    @field:NotNull(message = "Password is required")
    @field:NotBlank(message = "Password must not be blank")
    var password: String?
)
