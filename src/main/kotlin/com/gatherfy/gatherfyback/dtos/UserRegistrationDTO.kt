package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class UserRegistrationDTO(
    var registrationId: Long,
    var eventId: Long?,
    var name: String,
    var description: String,
    var detail: String,
    var start_date: LocalDateTime,
    var end_date: LocalDateTime,
    var location: String,
    var status: String,
    var slug: String,
    var image: String,
    var owner: String,
    var tags: List<String>?,
    var regisDate: LocalDateTime,
)
