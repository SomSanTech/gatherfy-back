package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventViewsDTO
import com.gatherfy.gatherfyback.dtos.ViewDTO
import com.gatherfy.gatherfyback.dtos.ViewData
import com.gatherfy.gatherfyback.entities.View
import com.gatherfy.gatherfyback.repositories.ViewRepository
import org.springframework.stereotype.Service


@Service
class ViewService(private val viewRepository: ViewRepository) {
    fun getViewsByEventIds(eventIds: List<Long>): List<EventViewsDTO> {
        val views = viewRepository.findViewsByEventIds(eventIds)
        return eventIds.map { eventId ->
            val filteredViews = views.filter { it.eventId == eventId }
            EventViewsDTO(
                event_id = eventId,
                views = filteredViews.map {
                    ViewData(
                        date = it.view_date.toString(),
                        count = it.view_count
                    )
                }
            )
        }
    }
    private fun toViewDTO(view: View): ViewDTO {
        return ViewDTO(
            view_id = view.view_id,
            event_id = view.eventId,
            view_date = view.view_date,
            view_count =view.view_count
        )
    }
}
