package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Feedback
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {

    fun findFeedbacksByEventId(eventId: Long): List<Feedback>

    fun findByUserIdAndEventId(userId: Long?, eventId: Long?): Feedback?

    @Query("SELECT f FROM feedbacks f JOIN events e ON f.eventId = e.event_id WHERE e.event_owner = :ownerId")
    fun findFeedbacksByOwnerId(ownerId: Long): List<Feedback>
}