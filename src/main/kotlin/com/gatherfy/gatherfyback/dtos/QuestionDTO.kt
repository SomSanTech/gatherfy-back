package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class QuestionDTO(
    var questionId: Long?,
    var eventId: Long?,
    var questionText: String?,
    var questionType: String?,
)
