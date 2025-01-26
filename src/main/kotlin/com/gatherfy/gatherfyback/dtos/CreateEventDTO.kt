package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateEventDTO (
    @field:NotNull(message = "Event name is required")
    @field:NotBlank(message = "Name must not be blank")
    var event_name: String?,

    @field:NotNull(message = "Event description is required")
    @field:NotBlank(message = "Description must not be blank")
    var event_desc: String?,

    @field:NotNull(message = "Event detail is required")
    @field:NotBlank(message = "Detail must not be blank")
    var event_detail: String?,

    @field:NotNull(message = "Event start date is required")
    @field:Future(message = "Event start date must be in the future")
    var event_start_date: LocalDateTime?,

    @field:NotNull(message = "Event end date is required")
    @field:Future(message = "Event end date must be in the future")
    var event_end_date: LocalDateTime?,

    @field:NotNull(message = "Event ticket start date is required")
    @field:Future(message = "Event ticket start date must be in the future")
    var event_ticket_start_date: LocalDateTime?,

    @field:NotNull(message = "Event ticket start date is required")
    @field:Future(message = "Event ticket start date must be in the future")
    var event_ticket_end_date: LocalDateTime?,

    @field:NotNull(message = "Event registration goal is required")
    var event_registration_goal: Long?,

    @field:NotNull(message = "Event location is required")
    @field:NotBlank(message = "Location must not be blank")
    var event_location: String?,

    @field:NotNull(message = "Event map is required")
    @field:NotBlank(message = "Map must not be blank")
    var event_google_map: String?,

    @field:NotNull(message = "Event capacity is required")
    var event_capacity: Long?,

    @field:NotNull(message = "Event slug is required")
    @field:NotBlank(message = "Slug must not be blank")
    var event_slug: String?,

    @field:NotNull(message = "Event image is required")
    @field:NotBlank(message = "Image must not be blank")
    var event_image: String?,
    var event_owner: Long?,

    @field:NotNull(message = "Event tag is required")
    var tags: List<Long>?,
)