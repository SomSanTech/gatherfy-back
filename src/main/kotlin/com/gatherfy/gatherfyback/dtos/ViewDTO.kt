package com.gatherfy.gatherfyback.dtos

import java.time.LocalDate

class ViewDTO(
    val view_id: Long?,
    val event_id: Long?,
    val view_date: LocalDate,
    val view_count: Long,
)

data class EventViewsDTO(
    val event_id: Long,
    val views: List<ViewData>
)

data class ViewData(
    val date: String,
    val count: Long
)
