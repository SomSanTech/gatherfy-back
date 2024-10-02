package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.repositories.EventRepository
import org.springframework.stereotype.Service

@Service
class EventService(val eventRepository: EventRepository) {

    fun getAllEvents() : List<Event> {
        return eventRepository.findAll()
    }

    fun getEventBySlug(slug : String) : Event {
        return eventRepository.findEventBySlug(slug)
    }

    fun getEventByKeyword(keyword: String) : List<Event> {
        return eventRepository.findEventByKeyword(keyword)
    }
}