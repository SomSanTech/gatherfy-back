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
        @RequestParam(required = false, defaultValue = "eventStartDate") sortField: String,
        @RequestParam(required = false, defaultValue = "desc") sortDirection: String
    ): List<EventDTO> {
        return if(!keyword.isNullOrEmpty() ) { //Have Keyword Have Sort
            eventService.getEventsByKeywordAndSort(keyword, sortField, sortDirection)
        } else{
            eventService.getAllEventsSorted(sortField, sortDirection)// Not Keyword Not Sort
        }
    }

    @GetMapping("/v1/events/{slug}")
    fun getEvent(@PathVariable slug: String) : EventDTO {
        return eventService.getEventBySlug(slug)
    }
}