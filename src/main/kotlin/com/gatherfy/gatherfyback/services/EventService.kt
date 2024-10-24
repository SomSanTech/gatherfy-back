package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventDTO
import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class EventService(
    val eventRepository: EventRepository,
    private val minioClient: MinioClient,
    private val userRepository: UserRepository
) {

//    fun getAllEvents() : List<EventDTO> {
//        return eventRepository.findAll().map { event ->
//            toEventDto(event) // Use toEventDto() to convert the event to EventDTO
//        }
//    }

    fun getAllEventsSorted(sortField: String, sortDirection: String): List<EventDTO> {
        val sort = if (sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by(Sort.Order.asc(sortField))
        } else {
            Sort.by(Sort.Order.desc(sortField))
        }
        val events = eventRepository.findAll(sort)
        return events.map { event -> toEventDto(event) }
    }

    fun getEventBySlug(slug : String) : EventDTO {
        return toEventDto(eventRepository.findEventBySlug(slug))
    }

//    fun getEventByKeyword(keyword: String) : List<EventDTO> {
//        return eventRepository.findEventByKeyword(keyword).map { event ->
//            toEventDto(event)
//        }
//    }

    fun getEventsByKeywordAndSort(keyword: String, sortField: String?, sortDirection: String?): List<EventDTO> {
        val sort = if (!sortField.isNullOrEmpty() && sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by(Sort.Order.asc(sortField))
        } else if (!sortField.isNullOrEmpty()) {
            Sort.by(Sort.Order.desc(sortField))
        } else {
            Sort.unsorted() // ถ้าไม่มี sortField จะไม่เรียงลำดับ
        }

        val events = eventRepository.findEventByKeyword(keyword, sort)
        return events.map { event -> toEventDto(event) }
    }

    fun toEventDto(event: Event) : EventDTO {
        val ownerEventName: String = userRepository.findById(event.eventOwner).map {
            it.username
        }.orElse("Unknown Organizer")
        return EventDTO(
            name = event.eventName,
            description = event.eventDesc,
            detail = event.eventDetail,
            start_date =  event.eventStartDate,
            end_date = event.eventEndDate,
            location = event.eventLocation,
            map = event.eventGoogleMap,
            capacity = event.eventCapacity,
            status = event.eventStatus,
            slug = event.event_slug,
            image =  getImageUrl(event.eventImage, "thumnails"),
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