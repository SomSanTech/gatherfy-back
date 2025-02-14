package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateAnswerDTO (
//    var feedbackId: Long,
    var questionId: Long,
//    var eventId: Long,
    @field:NotNull(message = "Answer is required")
    @field:NotBlank(message = "Answer must not be blank")
    var answerText: String?,
)