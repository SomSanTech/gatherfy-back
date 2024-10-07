package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.services.EventService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/events")
class EventController(var eventService: EventService) {

    @GetMapping("")
    fun getAllEvents(): List<EventDTO> {
        return eventService.getAllEvents()
    }

    @GetMapping("/{slug}")
    fun getEvent(@PathVariable slug: String) : EventDTO {
        return eventService.getEventBySlug(slug)
    }

    @GetMapping("/search")
    fun getEventSearch(@RequestParam keyword: String) : List<EventDTO> {
        return eventService.getEventByKeyword((keyword))
    }
}