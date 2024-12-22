package com.gatherfy.gatherfyback.dtos


data class CreateQuestionDTO(
    var eventId: Long?,
    var questionText: String?,
    var questionType: String?,
)
