package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.AccessDeniedException
import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.SortOption
import com.gatherfy.gatherfyback.entities.Tag
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.EventTagRepository
import com.gatherfy.gatherfyback.repositories.TagRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EventService(
    val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    val eventTagRepository: EventTagRepository,
    val eventTagService: EventTagService,
    val minioService: MinioService,
    private val emailSenderService: EmailSenderService,
    private val tagRepository: TagRepository,
) {
    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun getEventBySlug(slug: String): EventDTO {
        try {
            return toEventDto(eventRepository.findEventBySlug(slug))
        } catch (ex: Exception) {
            throw EntityNotFoundException("Event slug $slug does not exist")
        }
    }

    fun getEventById(id: Long): EventDTO {
        try{
            return toEventDto(eventRepository.findEventByEventId(id)!!)
        }
        catch (ex: Exception){
            throw EntityNotFoundException("Event id $id does not exist")
        }
    }

    fun getEventByIdWithAuth(username: String, id: Long): EventDTO {
        try{
            val user = userRepository.findByUsername(username)
            val event = eventRepository.findEventByEventId(id)
            if(event === null){
                throw EntityNotFoundException("Event id $id does not exist")
            }
            val isOwner = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, id)
            if(isOwner === null){
                throw AccessDeniedException("You are  not owner of this event")
            }
            return toEventDto(event)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }

    fun getEventFullTagById(id: Long): EventFullTagDTO {
        try{
            return toEventFullTagDto(eventRepository.findEventByEventId(id)!!)
        }
        catch (ex: Exception){
            throw EntityNotFoundException("Event id $id does not exist")
        }
    }

    fun getEventFullTagByIdWithAuth(username: String, id: Long): EventFullTagDTO {
        try{
            val user = userRepository.findByUsername(username)
            val event = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, id)
            if(event === null){
                throw EntityNotFoundException("Event id $id does not exist")
            }
            return toEventFullTagDto(event)
        }
        catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
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
        }catch (ex: Exception){
            throw EntityNotFoundException("Events do not exist")
        }
    }

    fun getEventByOwner(ownerId: Long?): List<EventRegistrationDTO> {
        try {
            val events = eventRepository.findEventsByEventOwner(ownerId)
            return events.map { toEventRegistrationDTO(it) }
        }
        catch (ex: Exception){
            throw EntityNotFoundException("User id $ownerId does not exist")
        }
    }

    fun getEventWithAuth(username: String): List<EventRegistrationDTO> {
        try {
            val user = userRepository.findByUsername(username)
            val events = eventRepository.findEventsByEventOwner(user?.users_id)
            return events.map { toEventRegistrationDTO(it) }
        }
        catch (ex: Exception){
            throw EntityNotFoundException("User $username does not exist")
        }
    }

    fun getRecommendedEvent(limit: Int): List<EventDTO> {
        val pageable = PageRequest.of(0, limit) // LIMIT functionality
        val recommend = eventRepository.findTopEvents(pageable)
        // Fetch full event details for each eventId and map it to RecommendEventDTO
        return recommend.map { item ->
            // Fetch the full Event entity using the eventId
            val event = eventRepository.findById(item.eventId).orElseThrow {
                EntityNotFoundException(" Event id ${item.eventId} does not exist.")
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
        } catch (e: MethodArgumentNotValidException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun createEventWithAuth(username: String, event: CreateEventDTO): Event {
        try {
            val user = userRepository.findByUsername(username)
            val duplicateEventName = eventRepository.findDuplicateEventName(event.event_name!!)
            val duplicateSlug = eventRepository.findDuplicateSlug(event.event_slug!!)
            val slugPattern = event.event_slug!!.matches("^[a-z0-9]+(-[a-z0-9]+)*\$".toRegex())

            if(duplicateEventName != null){
                throw ConflictException("Event name already taken")
            }
            else if(duplicateSlug != null){
                throw ConflictException("Event slug already taken")
            }
            else if(!slugPattern){
                throw BadRequestException("Slug should match pattern example-event-slug")
            }
            else{
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
                    event_owner = user?.users_id!!,
                    created_at = LocalDateTime.now(),
                    event_status = "soon"
                )
                val savedEvent = eventRepository.save(evente)
                if (event.tags!!.isNotEmpty()) {
//                    eventTagService.createEventTag(savedEvent.event_id!!, event.tags!!)
                    // Explicitly refresh and set tags
                    val updatedTags = tagRepository.findAllById(event.tags!!)
                    savedEvent.tags = updatedTags.toMutableList()
                    eventRepository.save(savedEvent) // Ensure Hibernate knows about it
                }
                emailSenderService.enqueueEmailNewEvent(savedEvent)
                return savedEvent
            }
        } catch (e: ConflictException) {
            throw ConflictException(e.message!!)
        } catch (e: BadRequestException) {
            throw BadRequestException(e.message)
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }
    // Helper method for additional validation
//    private fun validateEvent(event: CreateEventDTO) {
//        if (event.event_start_date != null && event.event_end_date != null) {
//            if (event.event_start_date.isAfter(event.event_end_date)) {
//                throw IllegalArgumentException("Event start date must be before the end date")
//            }
//        }
//
//        if (event.event_ticket_start_date != null && event.event_ticket_end_date != null) {
//            if (event.event_ticket_start_date.isAfter(event.event_ticket_end_date)) {
//                throw IllegalArgumentException("Ticket sale start date must be before the end date")
//            }
//        }
//    }
    fun updateEvent(eventId: Long, updateData: CreateEventDTO): Event {
        try {
            val event = eventRepository.findById(eventId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: $eventId") }

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
        } catch (e: MethodArgumentNotValidException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update event")
        }
    }

    fun updateEventPartialField(username: String, eventId: Long, updateData: EditEventDTO): Event {
        try {
            val user = userRepository.findByUsername(username)
            val exitingEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, eventId)
            if(exitingEvent === null){
                throw EntityNotFoundException("Event id $eventId does not exist")
            }

            if(!updateData.event_name.isNullOrBlank()){
                val duplicateEventName = eventRepository.findDuplicateEventName(updateData.event_name!!)
                if(duplicateEventName != null && duplicateEventName.event_id != eventId){
                    throw ConflictException("Event name already taken")
                }
            }
            if(!updateData.event_slug.isNullOrBlank()){
                val duplicateSlug = eventRepository.findDuplicateSlug(updateData.event_slug!!)
                val slugPattern = updateData.event_slug!!.matches("^[a-z0-9]+(-[a-z0-9]+)*\$".toRegex())

                if(duplicateSlug != null && duplicateSlug.event_id != eventId){
                    throw ConflictException("Event slug already taken")
                }
                else if(!slugPattern){
                    throw BadRequestException("Slug should match pattern example-event-slug")
                }
            }
            val changes = mutableListOf<String>()
            if(updateData.event_start_date != null && exitingEvent.event_start_date != updateData.event_start_date){
                val formatDateTime = updateData.event_start_date!!.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))
                changes.add("New Start Date/Time: $formatDateTime")
            }
            if(updateData.event_end_date != null &&exitingEvent.event_end_date != updateData.event_end_date){
                val formatDateTime = updateData.event_end_date!!.format(DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm"))
                changes.add("New End Date/Time: $formatDateTime")
            }
            if(updateData.event_location != null &&exitingEvent.event_location != updateData.event_location){
                changes.add("New Location: ${updateData.event_location}")
            }
            val updateEvent = exitingEvent.copy(
                event_name = updateData.event_name ?: exitingEvent.event_name,
                event_desc = updateData.event_desc ?: exitingEvent.event_desc,
                event_detail = updateData.event_detail ?: exitingEvent.event_detail,
                event_start_date = updateData.event_start_date ?: exitingEvent.event_start_date,
                event_end_date = updateData.event_end_date ?: exitingEvent.event_end_date,
                event_ticket_start_date = updateData.event_ticket_start_date ?: exitingEvent.event_ticket_start_date,
                event_ticket_end_date = updateData.event_ticket_end_date ?: exitingEvent.event_ticket_end_date,
                event_registration_goal = updateData.event_registration_goal ?: exitingEvent.event_registration_goal ,
                event_location = updateData.event_location ?: exitingEvent.event_location,
                event_google_map = updateData.event_google_map ?: exitingEvent.event_google_map,
                event_capacity = updateData.event_capacity ?: exitingEvent.event_capacity,
                event_slug = updateData.event_slug ?: exitingEvent.event_slug,
                event_image = updateData.event_image ?: exitingEvent.event_image
            )

            val updatedEvent = eventRepository.save(updateEvent)

            if (updateData.tags != null) {
                eventTagService.updatedTag(updatedEvent.event_id!!, updateData.tags!!)
            }
            // If there are changes, send an email
            if (changes.isNotEmpty()) {
                val notificationMessage = changes.joinToString("\n") // Combine all changes
                emailSenderService.sendUpdatedEventBatchEmails(updatedEvent, notificationMessage)
            }
            return updatedEvent
        } catch (e: ConflictException) {
            throw ConflictException(e.message!!)
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: BadRequestException) {
            throw BadRequestException(e.message)
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    fun deleteEvent(eventId: Long) {
        try{
            val event = eventRepository.findById(eventId)
                .orElseThrow { EntityNotFoundException("Event id $eventId does not exist") }
            val existingEventTags = eventTagRepository.findAllByEvent(event)
            eventTagRepository.deleteAll(existingEventTags)
            minioService.deleteFile("thumbnails",event.event_image)
            eventRepository.delete(event)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    fun deleteEventWithAuth(username: String, eventId: Long) {
        try{
            val user = userRepository.findByUsername(username)
            val exitingEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, eventId)
            if(exitingEvent === null){
                throw EntityNotFoundException("Event id $eventId does not exist")
            }
            val existingEventTags = eventTagRepository.findAllByEvent(exitingEvent)
            eventTagRepository.deleteAll(existingEventTags)
            minioService.deleteFile("thumbnails",exitingEvent.event_image)
            eventRepository.delete(exitingEvent)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    private fun toEventRegistrationDTO(event: Event): EventRegistrationDTO {
        return EventRegistrationDTO(
            eventId = event.event_id,
            eventName = event.event_name,
            eventLocation = event.event_location,
            eventStartDate = event.event_start_date,
            eventEndDate = event.event_end_date
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
            tags = event.tags?.map { toTagDTO(it) })
    }

    fun toTagDTO(tag: Tag): Tag {
        return Tag(
            tag_id = tag.tag_id,
            tag_title = tag.tag_title,
            tag_code = tag.tag_code
        )
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