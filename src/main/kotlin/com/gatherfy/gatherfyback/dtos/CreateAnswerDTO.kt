package com.gatherfy.gatherfyback.dtos

data class CreateAnswerDTO (
    var feedbackId: Long,
    var questionId: Long,
    var eventId: Long,
    var answerText: String,
)