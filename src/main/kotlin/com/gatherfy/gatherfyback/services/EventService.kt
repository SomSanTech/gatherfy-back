package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.stereotype.Service

@Service
class EventService(
    val eventRepository: EventRepository,
    private val minioClient: MinioClient,
    private val userRepository: UserRepository,
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

    fun getEventsByTagsTitle(tags: List<String>) : List<EventDTO> {
        return eventRepository.findEventsByTags(tags).map { event ->
            toEventDto(event)
        }
    }

    fun toEventDto(event: Event) : EventDTO {
        val ownerEventName: String = userRepository.findById(event.event_owner).map {
            it.username
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