package com.gatherfy.gatherfyback.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EditQuestionDTO(
    @field:NotNull(message = "Question is required")
    @field:NotBlank(message = "Question must not be blank")
    var questionText: String?,
    @field:NotNull(message = "Question type is required")
    @field:NotBlank(message = "Question type must not be blank")
    var questionType: String?,
)
