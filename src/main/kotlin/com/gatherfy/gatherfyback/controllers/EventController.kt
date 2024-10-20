package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.services.EventService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["*"])
class EventController(var eventService: EventService) {

    @GetMapping("/v1/events")
    fun getAllEvents(
        @RequestParam(required = false) keyword: String?,
    ): List<EventDTO> {
        return if(keyword.isNullOrEmpty()) {
            eventService.getAllEvents()
        } else {
            eventService.getEventByKeyword(keyword)
        }
    }

    @GetMapping("/v1/events/{slug}")
    fun getEvent(@PathVariable slug: String) : EventDTO {
        return eventService.getEventBySlug(slug)
    }
}