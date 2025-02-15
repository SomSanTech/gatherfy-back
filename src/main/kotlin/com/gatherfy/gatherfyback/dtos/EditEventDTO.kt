package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class EditEventDTO(
    @field:Pattern(regexp = "\\S.*", message = "Name must not be blank")
    @field:Size(max = 150)
    var event_name: String?,

    @field:Pattern(regexp = "\\S.*",message = "Description must not be blank")
    @field:Size(max = 160)
    var event_desc: String?,

    @field:Pattern(regexp = "\\S.*",message = "Detail must not be blank")
    var event_detail: String?,

    @field:Future(message = "Event start date must be in the future")
    var event_start_date: LocalDateTime?,

    @field:Future(message = "Event end date must be in the future")
    var event_end_date: LocalDateTime?,

    @field:Future(message = "Event ticket start date must be in the future")
    var event_ticket_start_date: LocalDateTime?,

    @field:Future(message = "Event ticket end date must be in the future")
    var event_ticket_end_date: LocalDateTime?,

    var event_registration_goal: Long?,

    @field:Pattern(regexp = "\\S.*",message = "Location must not be blank")
    var event_location: String?,

    @field:Pattern(regexp = "\\S.*",message = "Map must not be blank")
    var event_google_map: String?,

    var event_capacity: Long?,

    @field:Pattern(regexp = "\\S.*",message = "Slug must not be blank")
    @field:Size(max = 100)
    var event_slug: String?,

    @field:Pattern(regexp = "\\S.*",message = "Image must not be blank")
    @field:Size(max = 100)
    var event_image: String?,

    var tags: List<Long>?,
)
