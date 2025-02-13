package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.EventTag
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.EventTagRepository
import com.gatherfy.gatherfyback.repositories.TagRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class EventTagService(
    val eventTagRepository: EventTagRepository,
    val eventRepository: EventRepository,
    val tagRepository: TagRepository
) {

    fun createEventTag(eventId: Long, tags: List<Long>) {
        try {
            val event = eventRepository.findById(eventId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: $eventId") }

            val tagEntities = tagRepository.findAllById(tags)
            if (tagEntities.size != tags.size) {
                val missingTags = tags.minus(tagEntities.map { it.tag_id })
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Tags not found: $missingTags")
            }

            val eventTags = tagEntities.map { tag ->
                EventTag(event = event, tag = tag)
            }

            eventTagRepository.saveAll(eventTags)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create event-tag relationships")
        }
    }

    fun updatedTag(eventId: Long, tags: List<Long>) {
        try {
            val event = eventRepository.findById(eventId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: $eventId") }

            val tagEntities = tagRepository.findAllById(tags)
            val missingTags = tags.minus(tagEntities.map { it.tag_id })
            if (missingTags.isNotEmpty()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Tags not found: $missingTags")
            }

            val existingEventTags = eventTagRepository.findAllByEvent(event)

            val tagsToRemove = existingEventTags.filter { it.tag.tag_id !in tags }
            eventTagRepository.deleteAll(tagsToRemove)

            val newTagsToAdd = tagEntities.filter { tagEntity ->
                existingEventTags.none { it.tag.tag_id == tagEntity.tag_id }
            }.map { tag -> EventTag(event = event, tag = tag) }

            eventTagRepository.saveAll(newTagsToAdd)

        } catch (e: Exception) {
            throw when (e) {
                is ResponseStatusException -> e
                else -> ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update event-tag relationships", e)
            }
        }
    }

}