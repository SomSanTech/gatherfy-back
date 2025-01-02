package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Answer
import com.gatherfy.gatherfyback.entities.Feedback
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AnswerRepository: JpaRepository<Answer, Long> {

    fun findAnswersByFeedbackId(feedbackId: Long): List<Answer>

    @Query("SELECT a FROM answers a JOIN events e ON a.eventId = e.event_id WHERE e.event_owner = :ownerId")
    fun findAnswersByOwnerId(ownerId: Long): List<Answer>

    fun findAnswersByQuestionId(questionId: Long): List<Answer>
}