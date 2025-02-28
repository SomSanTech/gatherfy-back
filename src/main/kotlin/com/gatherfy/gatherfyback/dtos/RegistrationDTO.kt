package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime
import java.time.ZonedDateTime

class RegistrationDTO(
    val registrationId: Long,
    val eventName: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val gender: String?,
    val dateOfBirth: LocalDateTime?,
    val age: Long?,
    val email: String,
    val phone: String?,
    val status: String,
    val createdAt: ZonedDateTime
)