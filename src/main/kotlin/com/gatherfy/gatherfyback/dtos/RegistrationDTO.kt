package com.gatherfy.gatherfyback.dtos

import java.time.ZonedDateTime

class RegistrationDTO (
    val registrationId: Long,
    val eventName: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val gender: String,
    val email: String,
    val phone: String,
    val status: String,
    val createdAt: ZonedDateTime
)