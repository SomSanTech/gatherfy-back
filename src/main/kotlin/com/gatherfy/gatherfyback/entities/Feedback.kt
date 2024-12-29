package com.gatherfy.gatherfyback.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name="feedbacks")
data class Feedback(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    var feedbackId: Long?=null,
    @Column(name = "event_id")
    var eventId: Long?,
    @Column(name = "user_id")
    var userId: Long?,
    @Column(name = "feedback_rating")
    var feedbackRating: Long?,
    @Column(name = "feedback_comment")
    var feedbackComment: String,
    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: LocalDateTime?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id" , insertable = false, updatable = false)
    @JsonIgnore
    var event: Event?=null,
){
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}