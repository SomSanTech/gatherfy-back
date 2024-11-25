package com.gatherfy.gatherfyback.dtos

import java.util.Date

class ViewDTO(
    val view_id: Long?,
    val event_id: Long?,
    val view_date: Date?,
    val view_count: Long?,
)