package com.gatherfy.gatherfyback.dtos


data class AnswerDTO(
    var answerId: Long?,
    var feedbackId: Long?,
    var questionId: Long?,
    var questionText: String?,
    var answerText: String?,
)
