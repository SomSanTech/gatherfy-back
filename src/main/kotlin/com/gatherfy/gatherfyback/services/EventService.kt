package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.dtos.EventRegistrationDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.SortOption
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class EventService(
    val eventRepository: EventRepository,
    private val userRepository: UserRepository,
) {
    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun getEventBySlug(slug : String) : EventDTO {
        try{
            return toEventDto(eventRepository.findEventBySlug(slug))
        } catch (ex: Exception){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.localizedMessage, ex)
        }
    }
    fun getEventById(id : Long) : EventDTO {
        return toEventDto(eventRepository.findEventByEventId(id))
    }
    // Search keyword, Filter tags and date, also Sort event
    fun getFilteredEvents(
        keyword: String?,
        tags: List<String>?,
        date: LocalDate?,
        sort: SortOption?
    ): List<EventDTO> {
        try{
            val events: List<Event> = when {
                !keyword.isNullOrEmpty() && !tags.isNullOrEmpty() && date != null -> {
                    eventRepository.findEventsByKeywordAndTagsAndDate(keyword, tags, date)
                }
                !keyword.isNullOrEmpty() && !tags.isNullOrEmpty() -> {
                    eventRepository.findEventsByKeywordAndTags(keyword, tags)
                }
                !keyword.isNullOrEmpty() && date != null -> {
                    eventRepository.findEventsByKeywordAndDate(keyword, date)
                }
                !tags.isNullOrEmpty() && date != null -> {
                    eventRepository.findEventsByTagsAndDate(tags, date)
                }
                !keyword.isNullOrEmpty() -> {
                    eventRepository.findEventByKeyword(keyword)
                }
                !tags.isNullOrEmpty() -> {
                    eventRepository.findEventsByTags(tags)
                }
                date != null -> {
                    eventRepository.findEventsByDate(date)
                }
                else -> {
                    eventRepository.findAll()
                }
            }

            // Sort based on the SortOption
            val sortedEvents = when (sort) {
                SortOption.date_asc -> events.sortedWith(
                    compareBy<Event> { it.event_start_date }.thenBy { it.event_end_date }.thenBy { it.event_name.lowercase() }
                )
                SortOption.date_desc -> events.sortedWith(
                    compareByDescending<Event> { it.event_start_date }.thenBy { it.event_end_date }.thenBy { it.event_name.lowercase() }
                )
                SortOption.name_asc -> events.sortedBy { it.event_name.lowercase() }
                SortOption.name_desc -> events.sortedByDescending { it.event_name.lowercase() }
                else -> events.sortedByDescending { it.created_at } // if no sorting, sort by date desc
            }
            // Mapping the list of events to EventDTO
            return sortedEvents.map { event ->
                toEventDto(event)
            }
        } catch (e: RuntimeException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.localizedMessage, e)
        }
    }

    fun getEventByOwner(ownerId:Long?): List<EventRegistrationDTO> {
       val events =  eventRepository.findEventsByEventOwner(ownerId)
        return events.map { toEventRegistrationDTO(it) }
    }

    fun getRecommendedEvent(limit: Int): List<EventDTO> {
        val pageable = PageRequest.of(0, limit) // LIMIT functionality
        val recommend = eventRepository.findTopEvents(pageable)
        // Fetch full event details for each eventId and map it to RecommendEventDTO
        return recommend.map { item ->
            // Fetch the full Event entity using the eventId
            val event = eventRepository.findById(item.eventId).orElseThrow {
                EntityNotFoundException("Event not found for ID ${item.eventId}")
            }
            toEventDto(event)
        }
    }

    private fun toEventRegistrationDTO(event: Event):EventRegistrationDTO{
        return EventRegistrationDTO(
            eventId = event.event_id,
            eventName = event.event_name,
            eventLocation = event.event_location,
            eventStartDate = event.event_start_date,
        )
    }

    fun toEventDto(event: Event) : EventDTO {
        val ownerEventName: String = userRepository.findById(event.event_owner).map {
            it.username
        }.orElse("Unknown Organizer")
        return EventDTO(
            eventId = event.event_id,
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
            image =  getImageUrl("thumbnails", event.event_image),
            owner = ownerEventName,
            tags = event.tags?.map { it.tag_title }
        )
    }

    fun getImageUrl( bucketName: String, objectName: String): String {
        return "$minioDomain/$bucketName/$objectName"
    }
}