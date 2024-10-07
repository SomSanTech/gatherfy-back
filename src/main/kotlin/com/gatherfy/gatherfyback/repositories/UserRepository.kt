package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository: JpaRepository<User, Long> {
     override fun findById(organizer: Long): Optional<User>
}