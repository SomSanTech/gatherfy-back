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

//    Use this and delete "/v1/events/backoffice/{id}", "/v2/events/backoffice/{id}"
    @GetMapping("/v1/backoffice/events/{eventId}")
    fun getEvent(@RequestHeader("Authorization")token: String, @PathVariable eventId: String) : EventDTO {
    val id = eventId.toLongOrNull()
        ?: throw BadRequestException("Invalid event ID format")
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return eventService.getEventByIdWithAuth(username, id)
    }

//    @GetMapping("/v1/events/backoffice/{id}")
//    fun getEventOld(@PathVariable id: Long) : EventDTO {
//        return eventService.getEventById(id)
//    }

//    @GetMapping("/v2/events/backoffice/{id}")
//    fun getEventWithFullTagOld(@PathVariable id: Long) : EventFullTagDTO {
//        return eventService.getEventFullTagById(id)
//    }

    @GetMapping("/v2/backoffice/events/{eventId}")
    fun getEventWithFullTag(@RequestHeader("Authorization")token: String, @PathVariable eventId: String) : EventFullTagDTO {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return eventService.getEventFullTagByIdWithAuth(username, id)
    }

//    Use this instead of "/v1/backoffice/events/owner/{ownerId}", "/v1/events/owner/{ownerId}"
    @GetMapping("/v1/backoffice/events")
    fun getEventWithAuth(@RequestHeader("Authorization")token: String): List<EventRegistrationDTO> {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return eventService.getEventWithAuth(username)
    }

//    @GetMapping("/v1/backoffice/events/owner/{ownerId}")
//    fun getOwnerEvent(@PathVariable ownerId: Long?): List<EventRegistrationDTO> {
//        return eventService.getEventByOwner(ownerId)
//    }

//    @GetMapping("/v1/events/owner/{ownerId}")
//    fun getOwnerEventOld(@PathVariable ownerId: Long?): List<EventRegistrationDTO> {
//        return eventService.getEventByOwner(ownerId)
//    }

    @GetMapping("/v1/events/recommended")
    fun getRecommendedEvent(@RequestParam(defaultValue = "5") limit: Int): List<EventDTO> {
        return eventService.getRecommendedEvent(limit)
    }

    @PostMapping("/v1/backoffice/events")
    fun createEvent(@RequestBody eventDTO: CreateEventDTO): Event {
        return eventService.createEvent(eventDTO)
    }

    @PostMapping("/v2/backoffice/events")
    fun createEvent(@RequestHeader("Authorization")token: String,@RequestBody @Valid eventDTO: CreateEventDTO): Event {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return eventService.createEventWithAuth(username,eventDTO)
    }

    @PutMapping("/v1/backoffice/events/{eventId}")
    fun updateEvent(@PathVariable eventId: Long,@RequestBody eventDTO: CreateEventDTO): Event {
        return eventService.updateEvent(eventId,eventDTO)
    }

    @PutMapping("/v2/backoffice/events/{eventId}")
    fun updateEventPartialField(@RequestHeader("Authorization")token: String,@PathVariable eventId: String,@RequestBody @Valid eventDTO: EditEventDTO): Event {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return eventService.updateEventPartialField(username, id,eventDTO)
    }

    @DeleteMapping("/v1/backoffice/events/{eventId}")
    fun deleteEvent(@PathVariable eventId: Long) {
        eventService.deleteEvent(eventId)
    }

    @DeleteMapping("/v2/backoffice/events/{eventId}")
    fun deleteEventWithAuth(@RequestHeader("Authorization")token: String, @PathVariable eventId: String) {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        eventService.deleteEventWithAuth(username, id)
    }

}