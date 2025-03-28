package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateFeedbackDTO
import com.gatherfy.gatherfyback.dtos.FeedbackCountDTO
import com.gatherfy.gatherfyback.dtos.FeedbackDTO
import com.gatherfy.gatherfyback.entities.Feedback
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.FeedbackRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import com.gatherfy.gatherfyback.Exception.AccessDeniedException
import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.repositories.RegistrationRepository

@Service
class FeedbackService(
    val feedbackRepository: FeedbackRepository,
    private val eventRepository: EventRepository,
    private val registrationRepository: RegistrationRepository
){

    fun getAllFeedbackByEventId(userId: Int, eventId: Long) : List<FeedbackDTO>{
        try{
            eventRepository.findEventByEventId(eventId) ?: throw EntityNotFoundException("Event id $eventId does not exist")
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId.toLong(), eventId)

            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            val feedbackList = feedbackRepository.findFeedbacksByEventId(eventId)
            return feedbackList.map { toFeedbackDTO(it) }
        }catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun getFeedbackAndCountByEventId(userId: Int, eventId: Long): FeedbackCountDTO {
        try{
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId.toLong(), eventId)
            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            val feedbackList = feedbackRepository.findFeedbacksByEventId(eventId)
            return FeedbackCountDTO(
                count = feedbackList.count(),
                feedback = feedbackList
            )
        }catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }

    fun createFeedbackWithAuth(userId: Long, createFeedback: CreateFeedbackDTO): Feedback {
        try {
            val event = eventRepository.findById(createFeedback.eventId)
                .orElseThrow { EntityNotFoundException("Event id ${createFeedback.eventId} does not exist") }
            val registration = registrationRepository.findRegistrationsByUserIdAndEventId(userId.toInt(),createFeedback.eventId.toInt())
            if (registration === null){
                throw AccessDeniedException("You are not attendee of this event")
            }
            val isFeedbackExist = feedbackRepository.findByUserIdAndEventId(userId, event.event_id)
            if (isFeedbackExist !== null){
                throw ConflictException("You already gave feedback for this event.")
            }
            val feedback = Feedback(
                eventId = event.event_id,
                userId = userId,
                feedbackComment = createFeedback.feedbackComment!!,
                feedbackRating = createFeedback.feedbackRating,
            )
            val savedFeedback = feedbackRepository.save(feedback)
            return savedFeedback
        } catch (e: AccessDeniedException) {
            throw AccessDeniedException(e.message!!)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: ConflictException){
            throw ConflictException(e.message!!)
        }
    }
    fun getEventAlreadyFeedbacked(userId: Long): Map<String, List<Long>>{
        val eventList = feedbackRepository.findFeedbacksByUserId(userId)
        return mapOf("eventId" to eventList!!.mapNotNull { it.eventId })
    }

    private fun toFeedbackDTO(feedback: Feedback): FeedbackDTO {
        return FeedbackDTO(
            feedbackId = feedback.feedbackId,
            eventId = feedback.eventId,
            eventName = feedback.event!!.event_name,
            userId = feedback.userId,
            username = feedback.user!!.username,
            feedbackComment = feedback.feedbackComment,
            feedbackRating = feedback.feedbackRating,
            createdAt = feedback.createdAt
        )
    }
}