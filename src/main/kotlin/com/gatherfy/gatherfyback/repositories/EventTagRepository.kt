package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.EventTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventTagRepository: JpaRepository<EventTag, Long> {
    fun findAllByEvent(event: Event): List<EventTag>
}