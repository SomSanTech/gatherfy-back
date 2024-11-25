package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.View
import org.springframework.data.jpa.repository.JpaRepository

interface ViewRepository: JpaRepository<View, Long> {
    fun findViewsByEventId(eventId: Long): List<View>
}