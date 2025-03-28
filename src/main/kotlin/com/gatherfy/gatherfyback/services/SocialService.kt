package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateSocialDTO
import com.gatherfy.gatherfyback.entities.Social
import com.gatherfy.gatherfyback.repositories.SocialRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.MethodArgumentNotValidException
import java.sql.SQLException

@Service
class SocialService(
    private val socialRepository: SocialRepository,
    private val userRepository: UserRepository,
) {
    fun getSocialLinks(userId: Long): List<Social>{
//        val user = userRepository.findByUsername(username)
        return socialRepository.findSocialsByUserId(userId)
    }

    @Transactional
    fun updateSocialLink(userId: Long, createSocialDTO: CreateSocialDTO): List<Social> {
        try{
            //        val user = userRepository.findByUsername(username)
            // Pre-validation: Ensure all social platforms are valid before performing any DB operations
            createSocialDTO.socialLinks.forEach { it.validatePlatform() }

            val existingSocials = socialRepository.findSocialsByUserId(userId).map { it.socialId  }
            val deleteMissing = existingSocials.filter { it !in createSocialDTO.socialLinks.map { ex -> ex.socialId }  }
            if(deleteMissing.isNotEmpty()){
                socialRepository.deleteAllById(deleteMissing)
            }
            val socials = createSocialDTO.socialLinks.forEach {
                if (it.socialId == null){
                    val newSocial = Social(
                        userId = userId,
                        socialPlatform = it.socialPlatform!!,
                        socialLink = it.socialLink!!
                    )
                    socialRepository.save(newSocial)
                } else {
                    val existSocial = socialRepository.findSocialByUserIdAndSocialId(userId,it.socialId!!)
                        ?: throw EntityNotFoundException("Social id does not exist")
                    existSocial.socialPlatform = it.socialPlatform!!
                    existSocial.socialLink = it.socialLink!!
                    socialRepository.save(existSocial)
                }
            }
            return socialRepository.findSocialsByUserId(userId)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: MethodArgumentNotValidException) {
            throw e
        } catch (e: Exception){
            throw BadRequestException(e.message)
        }
    }
}