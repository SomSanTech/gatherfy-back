package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Event
import com.gatherfy.gatherfyback.entities.EventTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EventTagRepository: JpaRepository<EventTag, Long> {
    fun findAllByEvent(event: Event): List<EventTag>
}