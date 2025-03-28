package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.SortOption
import com.gatherfy.gatherfyback.services.EventService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class EventController(
    var eventService: EventService,
    private val tokenService: TokenService
) {

    @GetMapping("/v1/events")
    fun getAllEvents(
        @RequestParam keyword: String?,
        @RequestParam tags: List<String>?,
        @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") date: LocalDate?,
        @RequestParam sort: SortOption?
    ): List<EventDTO> {
        return eventService.getFilteredEvents(keyword, tags, date, sort)
    }

    @GetMapping("/v1/events/{slug}")
    fun getEvent(@PathVariable slug: String) : EventDTO {
        return eventService.getEventBySlug(slug)
    }

    @GetMapping("/v1/backoffice/events/{eventId}")
    fun getEvent(@RequestHeader("Authorization")token: String, @PathVariable eventId: String) : EventDTO {
    val id = eventId.toLongOrNull()
        ?: throw BadRequestException("Invalid event ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return eventService.getEventByIdWithAuth(userId.toLong(), id)
    }

    @GetMapping("/v2/backoffice/events/{eventId}")
    fun getEventWithFullTag(@RequestHeader("Authorization")token: String, @PathVariable eventId: String) : EventFullTagDTO {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return eventService.getEventFullTagByIdWithAuth(userId.toLong(), id)
    }

    @GetMapping("/v1/backoffice/events")
    fun getEventWithAuth(@RequestHeader("Authorization")token: String): List<EventRegistrationDTO> {
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return eventService.getEventWithAuth(userId.toLong())
    }

    @GetMapping("/v1/events/recommended")
    fun getRecommendedEvent(@RequestParam(defaultValue = "5") limit: Int): List<EventDTO> {
        return eventService.getRecommendedEvent(limit)
    }

    @PostMapping("/v2/backoffice/events")
    fun createEvent(@RequestHeader("Authorization")token: String,@RequestBody @Valid eventDTO: CreateEventDTO): Event {
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return eventService.createEventWithAuth(userId.toLong(),eventDTO)
    }

    @PutMapping("/v2/backoffice/events/{eventId}")
    fun updateEventPartialField(@RequestHeader("Authorization")token: String,@PathVariable eventId: String,@RequestBody @Valid eventDTO: EditEventDTO): Event {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return eventService.updateEventPartialField(userId.toLong(), id,eventDTO)
    }

    @DeleteMapping("/v2/backoffice/events/{eventId}")
    fun deleteEventWithAuth(@RequestHeader("Authorization")token: String, @PathVariable eventId: String) {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        eventService.deleteEventWithAuth(userId.toLong(), id)
    }

}