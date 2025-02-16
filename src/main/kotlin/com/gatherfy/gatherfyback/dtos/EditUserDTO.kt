package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class EditUserDTO(
    @field:Pattern(regexp = "\\S.*", message = "Firstname must not be blank")
    @field:Size(max = 50)
    var firstname: String?,

    @field:Pattern(regexp = "\\S.*", message = "Lastname must not be blank")
    @field:Size(max = 50)
    var lastname: String?,

    @field:Pattern(regexp = "\\S.*", message = "Username must not be blank")
    @field:Size(max = 30)
    var username: String?,

    @field:Pattern(regexp = "\\S.*", message = "Gender must not be blank")
    var gender: String?,

    @field:Pattern(regexp = "\\S.*", message = "Email must not be blank")
    @field:Size(max = 100)
    var email: String?,

    @field:Pattern(regexp = "\\S.*", message = "Phone must not be blank")
    @field:Size(max = 10)
    var phone: String?,

    @field:Size(max = 100)
    var image: String?,
    var birthday: LocalDateTime?,

    @field:Pattern(regexp = "\\S.*", message = "Password must not be blank")
    var password: String?
)
