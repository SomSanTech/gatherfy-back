package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Registration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RegistrationRepository: JpaRepository<Registration,Long> {

    fun findRegistrationsByEventId(eventId : Long) : List<Registration>

    fun findByRegistrationId(registrationId: Long): Registration?

    @Query("SELECT r FROM registrations r JOIN r.event e WHERE e.event_owner = :eventOwner")
    fun findRegistrationsByEventOwner(@Param("eventOwner") eventOwner: Long): List<Registration>?

    fun findByEventIdAndUserId(eventId: Long, userId: Long): Registration?

    fun findRegistrationsByUserId(userId: Long): List<Registration>

    fun findRegistrationsByUserIdAndEventId(userId: Int, eventId: Int): Registration?

    @Query("SELECT r FROM registrations r JOIN r.event e where r.eventId = :eventId and e.event_owner = :ownerId and r.registrationId = :registrationId")
    fun findByOwnerIdAndEventId(ownerId: Long, eventId: Long, registrationId: Long): Registration?

    fun findByEventIdAndRegisDate(eventId: Long?, regisDate: LocalDate): List<Registration>?
}