package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.EventViewsDTO
import com.gatherfy.gatherfyback.dtos.ViewDTO
import com.gatherfy.gatherfyback.dtos.ViewData
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.entities.View
import com.gatherfy.gatherfyback.repositories.ViewRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*


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
                        date = it.viewDate.toString(),
                        count = it.viewCount
                    )
                }
            )
        }
    }

    @Transactional
    fun recordEventView(eventId: Long): String {
        val today = LocalDate.now()
        val result: Optional<View> = viewRepository.findByEventIdAndViewDate(eventId, today)

        if (result.isPresent) {
            val view = result.get()
            view.viewCount = (view.viewCount ?: 0) + 1
            viewRepository.save(view)
            return "view updated"
        } else {
            val viewData = View(
                eventId = eventId,
                viewDate = today,
                viewCount = 1
            )
            viewRepository.save(viewData)
            viewRepository.flush()
            return "no data"
        }
    }





    private fun toViewDTO(view: View): ViewDTO {
        return ViewDTO(
            view_id = view.viewId,
            event_id = view.eventId,
            view_date = view.viewDate,
            view_count =view.viewCount,
        )
    }
}
