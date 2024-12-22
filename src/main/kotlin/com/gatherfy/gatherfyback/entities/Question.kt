package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name="questions")
data class Question(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    var questionId: Long? = null,
    @Column(name = "event_id")
    var eventId: Long?,
    @Column(name = "question_text")
    var questionText: String?,
    @Column(name = "question_type")
    var questionType: String?,
)