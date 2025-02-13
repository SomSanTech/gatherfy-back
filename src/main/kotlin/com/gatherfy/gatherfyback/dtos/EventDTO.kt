package com.gatherfy.gatherfyback.dtos

import com.gatherfy.gatherfyback.entities.Tag
import java.time.LocalDateTime

data class EventDTO(
    var eventId: Long?,
    var name: String,
    var description: String,
    var detail: String,
    var start_date: LocalDateTime,
    var end_date: LocalDateTime,
    var ticket_start_date: LocalDateTime,
    var ticket_end_date: LocalDateTime,
    var registration_goal:Long,
    var location: String,
    var map: String,
    var capacity: Long,
    var status: String,
    var slug: String,
    var image: String,
    var owner: String,
    var tags: List<Tag>?
)
