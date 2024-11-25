package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.RegistrationDTO
import com.gatherfy.gatherfyback.dtos.ViewDTO
import com.gatherfy.gatherfyback.entities.View
import com.gatherfy.gatherfyback.repositories.ViewRepository
import org.springframework.stereotype.Service
import java.util.*


@Service
class ViewService(private val viewRepository: ViewRepository) {
    fun getViewByEventId(viewId: Long): List<ViewDTO> {
        val view = viewRepository.findViewsByEventId(viewId)
        return view.map { toViewDTO(it) }
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
