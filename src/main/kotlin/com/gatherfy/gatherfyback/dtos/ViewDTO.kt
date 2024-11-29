package com.gatherfy.gatherfyback.dtos

import java.util.Date

class ViewDTO(
    val view_id: Long?,
    val event_id: Long?,
    val view_date: Date?,
    val view_count: Long?,
)

data class EventViewsDTO(
    val event_id: Long,
    val views: List<ViewData>
)

data class ViewData(
    val date: String,
    val count: Long?
)
