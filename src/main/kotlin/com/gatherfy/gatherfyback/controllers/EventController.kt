package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.services.EventService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["*"])
class EventController(var eventService: EventService) {

    @GetMapping("/v1/events")
    fun getAllEvents(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") date: LocalDate?,
    ): List<EventDTO> {
        return eventService.getFilteredEvents(keyword, tags, date)
    }

    @GetMapping("/v1/events/{slug}")
    fun getEvent(@PathVariable slug: String) : EventDTO {
        return eventService.getEventBySlug(slug)
    }
}