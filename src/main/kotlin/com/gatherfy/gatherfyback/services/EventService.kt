package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.dtos.EventRegistrationDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.SortOption
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EventService(
    val eventRepository: EventRepository,
    private val minioClient: MinioClient,
    private val userRepository: UserRepository,
) {

    fun getEventBySlug(slug : String) : EventDTO {
        return toEventDto(eventRepository.findEventBySlug(slug))
    }

    // Search keyword, Filter tags and date, also Sort event
    fun getFilteredEvents(
        keyword: String?,
        tags: List<String>?,
        date: LocalDate?,
        sort: SortOption?
    ): List<EventDTO> {
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
            SortOption.date_asc -> events.sortedBy { it.event_start_date }
            SortOption.date_desc -> events.sortedByDescending { it.event_start_date }
            SortOption.name_asc -> events.sortedBy { it.event_name }
            SortOption.name_desc -> events.sortedByDescending { it.event_name }
            SortOption.capacity_asc -> events.sortedBy { it.event_capacity }
            SortOption.capacity_desc -> events.sortedByDescending { it.event_capacity }
            else -> events.sortedByDescending { it.event_start_date } // if no sorting, sort by date desc
        }
        // Mapping the list of events to EventDTO
        return sortedEvents.map { event ->
            toEventDto(event)
        }
    }

    fun getEventByOwner(ownerId:Long?): List<EventRegistrationDTO> {
       val events =  eventRepository.findEventsByEventOwner(ownerId)
        return events.map { toEventRegistrationDTO(it) }
    }

    private fun toEventRegistrationDTO(event: Event):EventRegistrationDTO{
        return EventRegistrationDTO(
            eventId = event.event_id,
            eventName = event.event_name,
            eventLocation = event.event_location,
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
            image =  getImageUrl(event.event_image, "thumnails"),
            owner = ownerEventName,
            tags = event.tags?.map { it.tag_title }
        )
    }

    fun getImageUrl(objectName: String, bucketName: String): String{
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .method(Method.GET)
                .expiry(60*60)
                .build()
        )
    }
}