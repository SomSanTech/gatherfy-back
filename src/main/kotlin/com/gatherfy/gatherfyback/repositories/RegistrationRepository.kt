package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.dtos.RegistrationDTO
import com.gatherfy.gatherfyback.entities.Registration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegistrationRepository: JpaRepository<Registration,Long> {

    fun findRegistrationsByEventId(eventId : Long) : List<Registration>
}