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
    fun getSocialLinks(userId: Long): List<Social>{
//        val user = userRepository.findByUsername(username)
        return socialRepository.findSocialsByUserId(userId)
    }

    fun updateSocialLink(userId: Long, createSocialDTO: CreateSocialDTO){
//        val user = userRepository.findByUsername(username)
        val existingSocials = socialRepository.findSocialsByUserId(userId).map { it.socialId  }
        val deleteMissing = existingSocials.filter { it !in createSocialDTO.socialLinks.map { ex -> ex.socialId }  }
        if(deleteMissing.isNotEmpty()){
            socialRepository.deleteAllById(deleteMissing)
        }
        val socials = createSocialDTO.socialLinks.forEach {
            if (it.socialId == null){
                val newSocial = Social(
                    userId = userId.toLong(),
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