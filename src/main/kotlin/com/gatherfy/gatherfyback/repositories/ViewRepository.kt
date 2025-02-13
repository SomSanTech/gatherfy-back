package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.View
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.*

interface ViewRepository: JpaRepository<View, Long> {
    @Query("SELECT v FROM views v WHERE v.eventId IN :eventIds")
    fun findViewsByEventIds(@Param("eventIds") eventIds: List<Long>): List<View>

    @Query("SELECT v FROM views v WHERE v.eventId = :eventId AND v.viewDate = :viewDate")
    fun findByEventIdAndViewDate(@Param("eventId") eventId: Long, @Param("viewDate") viewDate: LocalDate): Optional<View>

}