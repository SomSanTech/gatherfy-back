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

    fun updateSocialLink(username: String, createSocialDTO: CreateSocialDTO){
        val user = userRepository.findByUsername(username)
        val existingSocials = socialRepository.findSocialsByUserId(user?.users_id!!).map { it.socialId  }
        val deleteMissing = existingSocials.filter { it !in createSocialDTO.socialLinks.map { ex -> ex.socialId }  }
        if(deleteMissing.isNotEmpty()){
            socialRepository.deleteAllById(deleteMissing)
        }
        val socials = createSocialDTO.socialLinks.forEach {
            if (it.socialId == null){
                val newSocial = Social(
                    userId = user?.users_id!!,
                    socialPlatform = it.socialPlatform,
                    socialLink = it.socialLink
                )
                socialRepository.save(newSocial)
            } else {
                val existSocial = socialRepository.findSocialBySocialId(it.socialId!!)
                existSocial.socialPlatform = it.socialPlatform
                existSocial.socialLink = it.socialLink
                socialRepository.save(existSocial)
            }
        }
    }
}