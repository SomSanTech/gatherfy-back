package com.gatherfy.gatherfyback.dtos

data class EventViewsDTO(
    val event_id: Long,
    val views: List<ViewData>
)

data class ViewData(
    val date: String,
    val count: Long
)
