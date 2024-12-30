package com.gatherfy.gatherfyback.dtos

import com.gatherfy.gatherfyback.entities.Feedback

data class FeedbackCountDTO(
    val count: Int,
    val feedback: List<Feedback>
)