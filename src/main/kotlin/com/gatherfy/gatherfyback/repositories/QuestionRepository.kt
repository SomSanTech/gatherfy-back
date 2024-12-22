package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository: JpaRepository<Question, Long> {

    fun findQuestionsByEventId(eventId: Long): List<Question>
}