package com.gatherfy.gatherfyback.dtos

import java.time.ZonedDateTime

data class EventDTO(
    var name: String,
    var description: String,
    var detail: String,
    var start_date: ZonedDateTime,
    var end_date: ZonedDateTime,
    var location: String,
    var map: String,
    var capacity: Long,
    var status: String,
    var slug: String,
    var image: String,
    var organizer: String
)
