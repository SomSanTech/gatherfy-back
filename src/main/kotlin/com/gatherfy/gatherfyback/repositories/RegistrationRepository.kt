package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Registration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RegistrationRepository: JpaRepository<Registration,Long> {

    fun findRegistrationsByEventId(eventId : Long) : List<Registration>

    @Query("SELECT r FROM registrations r JOIN r.event e WHERE e.event_owner = :eventOwner")
    fun findRegistrationsByEventOwner(@Param("eventOwner") eventOwner: Long): List<Registration>?

    fun findByEventIdAndUserId(eventId: Long, userId: Long): Registration?

    fun findRegistrationsByUserId(userId: Long): List<Registration>

    fun findRegistrationsByUserIdAndEventId(userId: Int, eventId: Int): Registration?
}