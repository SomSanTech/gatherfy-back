package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Social
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SocialRepository: JpaRepository <Social,Long> {
    fun findSocialsByUserId(userId: Long): List<Social>
}