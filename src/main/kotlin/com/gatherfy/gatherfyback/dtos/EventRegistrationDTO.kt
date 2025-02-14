package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

class EventRegistrationDTO(
    val eventId: Long?,
    val eventName: String,
    val eventLocation: String,
    val eventStartDate: LocalDateTime,
    val eventEndDate: LocalDateTime,
)