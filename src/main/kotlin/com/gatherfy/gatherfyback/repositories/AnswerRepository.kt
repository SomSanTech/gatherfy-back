package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Answer
import com.gatherfy.gatherfyback.entities.Feedback
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AnswerRepository: JpaRepository<Answer, Long> {

    fun findAnswersByFeedbackId(feedbackId: Long): List<Answer>

    @Query("SELECT a FROM answers a JOIN questions q ON a.questionId = q.questionId JOIN events e on q.eventId = e.event_id WHERE e.event_owner = :ownerId")
    fun findAnswersByOwnerId(ownerId: Long): List<Answer>

    fun findAnswersByQuestionId(questionId: Long): List<Answer>?

    @Query("from answers a join feedbacks f on a.feedbackId = f.feedbackId where f.eventId = :eventId")
    fun findAnswersByEventId(eventId: Long): List<Answer>?
}