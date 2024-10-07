package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class EventService(
    val eventRepository: EventRepository,
    val userRepository: UserRepository
) {

    fun getAllEvents() : List<EventDTO> {
        return eventRepository.findAll().map { event ->
            toEventDto(event) // Use toEventDto() to convert the event to EventDTO
        }
    }

    fun getEventBySlug(slug : String) : EventDTO {
        return toEventDto(eventRepository.findEventBySlug(slug))
    }

    fun getEventByKeyword(keyword: String) : List<EventDTO> {
        return eventRepository.findEventByKeyword(keyword).map { event ->
            toEventDto(event)
        }
    }

    fun toEventDto(event: Event) : EventDTO {
        val organizeName: String = userRepository.findById(event.event_organizer).map {
            it.user_organize_name
        }.orElse("Unknown Organizer")
        return EventDTO(
            name = event.event_name,
            description = event.event_desc,
            detail = event.event_detail,
            start_date =  event.event_start_date,
            end_date = event.event_end_date,
            location = event.event_location,
            map = event.event_google_map,
            capacity = event.event_capacity,
            status = event.event_status,
            slug = event.event_slug,
            image = event.event_image,
            organizer = organizeName
        )
    }
}