package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateFeedbackDTO(
    var eventId: Long,
    @field:Min(1)
    @field:Max(5)
    val feedbackRating: Int,
    @field:NotNull(message = "Feedback comment is required")
    @field:NotBlank(message = "Comment must not be blank")
    var feedbackComment: String?,
)
