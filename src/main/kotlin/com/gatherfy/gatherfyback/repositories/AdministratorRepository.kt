package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Administrator
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AdministratorRepository: JpaRepository<Administrator, Long> {
     override fun findById(organizer: Long): Optional<Administrator>
}