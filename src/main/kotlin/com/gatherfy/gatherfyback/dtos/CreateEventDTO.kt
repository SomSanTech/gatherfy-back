package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class CreateEventDTO (
    var event_name: String?,
    var event_desc: String?,
    var event_detail: String?,
    var event_start_date: LocalDateTime?,
    var event_end_date: LocalDateTime?,
    var event_ticket_start_date: LocalDateTime?,
    var event_ticket_end_date: LocalDateTime?,
    var event_registration_goal: Long?,
    var event_location: String?,
    var event_google_map: String?,
    var event_capacity: Long?,
    var event_slug: String?,
    var event_image: String?,
    var event_owner: Long?,
    var tags: List<Long>?,
)