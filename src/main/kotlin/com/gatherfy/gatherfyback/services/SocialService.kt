package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateSocialDTO
import com.gatherfy.gatherfyback.entities.Social
import com.gatherfy.gatherfyback.repositories.SocialRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class SocialService(
    private val socialRepository: SocialRepository,
    private val userRepository: UserRepository,
) {
    fun getSocialLinks(username: String): List<Social>{
        val user = userRepository.findByUsername(username)
        return socialRepository.findSocialsByUserId(user?.users_id!!)
    }

    fun createSocialLink(username: String, createSocialDTO: CreateSocialDTO){
        val user = userRepository.findByUsername(username)
        val socials = createSocialDTO.socialLinks.map {
            Social(
                userId = user?.users_id!!,
                socialPlatform = it.socialPlatform,
                socialLink = it.socialLink
            )
        }
        socialRepository.saveAll(socials)
    }
}