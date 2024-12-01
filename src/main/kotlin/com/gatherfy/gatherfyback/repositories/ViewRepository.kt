package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.View
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ViewRepository: JpaRepository<View, Long> {
    @Query("SELECT v FROM views v WHERE v.eventId IN :eventIds")
    fun findViewsByEventIds(@Param("eventIds") eventIds: List<Long>): List<View>

}