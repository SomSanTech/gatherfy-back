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
import com.gatherfy.gatherfyback.repositories.RegistrationRepository

@Service
class FeedbackService(
    val feedbackRepository: FeedbackRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val registrationRepository: RegistrationRepository
){
    fun getAllFeedback() : List<Feedback>{
        val feedbackList = feedbackRepository.findAll()
        return feedbackList
    }

    fun getAllFeedbackByEventId(username: String, eventId: Long) : List<FeedbackDTO>{
        try{
            val user = userRepository.findByUsername(username)
            val isEventExist = eventRepository.findEventByEventId(eventId)
                ?: throw EntityNotFoundException("Event id $eventId does not exist")
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, eventId)

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

    fun getFeedbackAndCountByEventId(username: String, eventId: Long): FeedbackCountDTO {
        try{
            val user = userRepository.findByUsername(username)

            val existEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, eventId)
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

    fun getAllFeedbackByOwner(ownerId: Long) : List<FeedbackDTO>{
        val feedbackList = feedbackRepository.findFeedbacksByOwnerId(ownerId)
        return feedbackList.map { toFeedbackDTO(it) }
    }

    fun createFeedback(createFeedback: Feedback): Feedback {
        try {
            val existingFeedback = feedbackRepository.findByUserIdAndEventId(
                createFeedback.userId,
                createFeedback.eventId
            )
            if (existingFeedback != null) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already feedback this event"
                )
            }
            val feedback = Feedback(
                eventId = createFeedback.eventId,
                userId = createFeedback.userId,
                feedbackComment = createFeedback.feedbackComment,
                feedbackRating = createFeedback.feedbackRating,
            )
            val savedFeedback = feedbackRepository.save(feedback)
            return savedFeedback
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun createFeedbackWithAuth(username: String, createFeedback: CreateFeedbackDTO): Feedback {
        try {
            val user = userRepository.findByUsername(username)
            val event = eventRepository.findById(createFeedback.eventId)
                .orElseThrow { EntityNotFoundException("Event id ${createFeedback.eventId} does not exist") }
            val registration = registrationRepository.findRegistrationsByUserIdAndEventId(user?.users_id!!.toInt(),createFeedback.eventId.toInt())

            if (registration === null){
                throw AccessDeniedException("You are not attendee of this event")
            }
            val feedback = Feedback(
                eventId = event.event_id,
                userId = user.users_id,
                feedbackComment = createFeedback.feedbackComment!!,
                feedbackRating = createFeedback.feedbackRating,
            )
            val savedFeedback = feedbackRepository.save(feedback)
            return savedFeedback
        } catch (e: AccessDeniedException) {
            throw AccessDeniedException(e.message!!)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun deleteFeedback(feedbackId: Long){
        try {
            val feedback = feedbackRepository.findById(feedbackId).orElseThrow {
                EntityNotFoundException("Feedback id $feedbackId does not exist")
            }
            feedbackRepository.delete(feedback)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
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