package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class FeedbackDTO (
    var feedbackId: Long?,
    var eventId: Long?,
    var eventName: String?,
    var userId: Long?,
    var username: String?,
    var feedbackRating: Long?,
    var feedbackComment: String,
    var createdAt: LocalDateTime?=null,
)