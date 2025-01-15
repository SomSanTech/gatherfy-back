package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateEventDTO
import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.dtos.EventFullTagDTO
import com.gatherfy.gatherfyback.dtos.EventRegistrationDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.EventTag
import com.gatherfy.gatherfyback.entities.SortOption
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.EventTagRepository
import com.gatherfy.gatherfyback.repositories.TagRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EventService(
    val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    val eventTagRepository: EventTagRepository,
    val eventTagService: EventTagService,
    val tagRepository: TagRepository,
    val minioService: MinioService,
) {
    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun getEventBySlug(slug: String): EventDTO {
        try {
            return toEventDto(eventRepository.findEventBySlug(slug))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, ex.localizedMessage, ex)
        }
    }

    fun getEventById(id: Long): EventDTO {
        return toEventDto(eventRepository.findEventByEventId(id))
    }

    fun getEventFullTagById(id: Long): EventFullTagDTO {
        return toEventFullTagDto(eventRepository.findEventByEventId(id))
    }

    // Search keyword, Filter tags and date, also Sort event
    fun getFilteredEvents(
        keyword: String?, tags: List<String>?, date: LocalDate?, sort: SortOption?
    ): List<EventDTO> {
        try {
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
                SortOption.date_asc -> events.sortedWith(compareBy<Event> { it.event_start_date }.thenBy { it.event_end_date }
                    .thenBy { it.event_name.lowercase() })

                SortOption.date_desc -> events.sortedWith(compareByDescending<Event> { it.event_start_date }.thenBy { it.event_end_date }
                    .thenBy { it.event_name.lowercase() })

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

    fun getEventByOwner(ownerId: Long?): List<EventRegistrationDTO> {
        val events = eventRepository.findEventsByEventOwner(ownerId)
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

    fun createEvent(event: CreateEventDTO): Event {
        try {
            val evente = Event(
                event_id = null,
                event_name = event.event_name!!,
                event_desc = event.event_desc!!,
                event_detail = event.event_detail!!,
                event_start_date = event.event_start_date!!,
                event_end_date = event.event_end_date!!,
                event_ticket_start_date = event.event_ticket_start_date!!,
                event_ticket_end_date = event.event_ticket_end_date,
                event_registration_goal = event.event_registration_goal,
                event_location = event.event_location!!,
                event_google_map = event.event_google_map!!,
                event_capacity = event.event_capacity!!,
                event_slug = event.event_slug!!,
                event_image = event.event_image!!,
                event_owner = event.event_owner!!,
                created_at = LocalDateTime.now(),
                event_status = "soon"
            )
            val savedEvent = eventRepository.save(evente)
            if (event.tags!!.isNotEmpty()) {
                eventTagService.createEventTag(savedEvent.event_id!!, event.tags!!)
            }
            return savedEvent
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun updateEvent(eventId: Long, updateData: CreateEventDTO): Event {
        try {
            val event = eventRepository.findById(eventId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: $eventId") }
            minioService.deleteFile(event.event_image)

            event.event_name = updateData.event_name!!
            event.event_desc = updateData.event_desc!!
            event.event_detail = updateData.event_detail!!
            event.event_start_date = updateData.event_start_date!!
            event.event_end_date = updateData.event_end_date!!
            event.event_ticket_start_date = updateData.event_ticket_start_date!!
            event.event_ticket_end_date = updateData.event_ticket_end_date!!
            event.event_registration_goal = updateData.event_registration_goal
            event.event_location = updateData.event_location!!
            event.event_google_map = updateData.event_google_map!!
            event.event_capacity = updateData.event_capacity!!
            event.event_slug = updateData.event_slug!!
            event.event_image = updateData.event_image!!
            event.event_owner = updateData.event_owner!!

            if (updateData.tags!!.isNotEmpty()) {
                eventTagService.updatedTag(event.event_id!!, updateData.tags!!)
            }

            val updatedEvent = eventRepository.save(event)
            return updatedEvent
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update event")
        }
    }

    fun deleteEvent(eventId: Long) {
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: $eventId") }
        val existingEventTags = eventTagRepository.findAllByEvent(event)
        eventTagRepository.deleteAll(existingEventTags)
        minioService.deleteFile(event.event_image)
        eventRepository.delete(event)
    }

    private fun toEventRegistrationDTO(event: Event): EventRegistrationDTO {
        return EventRegistrationDTO(
            eventId = event.event_id,
            eventName = event.event_name,
            eventLocation = event.event_location,
            eventStartDate = event.event_start_date,
        )
    }

    fun toEventDto(event: Event): EventDTO {
        val ownerEventName: String = userRepository.findById(event.event_owner).map {
            it.username
        }.orElse("Unknown Organizer")
        return EventDTO(eventId = event.event_id,
            name = event.event_name,
            description = event.event_desc,
            detail = event.event_detail,
            start_date = event.event_start_date,
            end_date = event.event_end_date,
            ticket_start_date = event.event_ticket_start_date,
            ticket_end_date = event.event_ticket_end_date!!,
            location = event.event_location,
            map = event.event_google_map,
            capacity = event.event_capacity,
            registration_goal = event.event_registration_goal!!,
            status = event.event_status,
            slug = event.event_slug,
            image = getImageUrl("thumbnails", event.event_image),
            owner = ownerEventName,
            tags = event.tags?.map { it.tag_title })
    }

    fun toEventFullTagDto(event: Event): EventFullTagDTO {
        val ownerEventName: String = userRepository.findById(event.event_owner).map {
            it.username
        }.orElse("Unknown Organizer")
        return EventFullTagDTO(
            eventId = event.event_id,
            name = event.event_name,
            description = event.event_desc,
            detail = event.event_detail,
            start_date = event.event_start_date,
            end_date = event.event_end_date,
            ticket_start_date = event.event_ticket_start_date,
            ticket_end_date = event.event_ticket_end_date!!,
            location = event.event_location,
            map = event.event_google_map,
            registration_goal = event.event_registration_goal!!,
            capacity = event.event_capacity,
            status = event.event_status,
            slug = event.event_slug,
            image = getImageUrl("thumbnails", event.event_image),
            owner = ownerEventName,
            tags = event.tags
        )
    }

    fun getImageUrl(bucketName: String, objectName: String): String {
        return "$minioDomain/$bucketName/$objectName"
    }
}