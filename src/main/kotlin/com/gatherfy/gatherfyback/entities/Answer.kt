package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name="answers")
data class Answer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    var answerId: Long? = null,

    @Column(name = "feedback_id", insertable = false,updatable = false)
    var feedbackId: Long,

    @Column(name = "question_id", insertable = false,updatable = false)
    var questionId: Long,

    @Column(name = "answer_text")
    var answerText: String?,

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    var feedback: Feedback,

    @ManyToOne
    @JoinColumn(name = "question_id")
    var question: Question,
)