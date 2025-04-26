package com.gatherfy.gatherfyback.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class CreateRegistrationDTO(
    val eventId: Long,
    val regisDate: LocalDate,
)
