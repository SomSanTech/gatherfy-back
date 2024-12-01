package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.dtos.EventRegistrationDTO
import com.gatherfy.gatherfyback.entities.SortOption
import com.gatherfy.gatherfyback.services.EventService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class EventController(var eventService: EventService) {

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

    @GetMapping("/v1/events/backoffice/{id}")
    fun getEvent(@PathVariable id: Long) : EventDTO {
        return eventService.getEventById(id)
    }

    @GetMapping("/v1/events/registration/{ownerId}")
    fun getOwnerEvent(@PathVariable ownerId: Long?): List<EventRegistrationDTO> {
        return eventService.getEventByOwner(ownerId)
    }
}