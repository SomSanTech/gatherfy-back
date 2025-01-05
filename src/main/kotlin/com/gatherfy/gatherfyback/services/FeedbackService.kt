package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.FeedbackCountDTO
import com.gatherfy.gatherfyback.dtos.FeedbackDTO
import com.gatherfy.gatherfyback.entities.Feedback
import com.gatherfy.gatherfyback.repositories.FeedbackRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FeedbackService (
    val feedbackRepository: FeedbackRepository
){
    fun getAllFeedback() : List<Feedback>{
        val feedbackList = feedbackRepository.findAll()
        return feedbackList
    }

    fun getAllFeedbackByEventId(eventId: Long) : List<FeedbackDTO>{
        val feedbackList = feedbackRepository.findFeedbacksByEventId(eventId)
        return feedbackList.map { toFeedbackDTO(it) }
    }

    fun getFeedbackAndCountByEventId(eventId: Long): FeedbackCountDTO {
        val feedbackList = feedbackRepository.findFeedbacksByEventId(eventId)
        return FeedbackCountDTO(
            count = feedbackList.count(),
            feedback = feedbackList
        )
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

    fun deleteFeedback(feedbackId: Long){
        try {
            val feedback = feedbackRepository.findById(feedbackId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found")
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