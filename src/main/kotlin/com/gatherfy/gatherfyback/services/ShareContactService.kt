package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.Contact
import com.gatherfy.gatherfyback.repositories.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ShareContactService(
    private val userRepository: UserRepository,
    private val socialRepository: SocialRepository,
    private val tokenService: TokenService,
    private val minioService: MinioService,
    private val contactRepository: ContactRepository,
    private val registrationRepository: RegistrationRepository,
    private val userService: UserService,
) {

    fun getContacts(username: String): List<ContactSavedDTO>? {
        val user = userRepository.findByUsername(username) ?: return null
        val contactList = contactRepository.findContactsByUserId(user.users_id!!)

        return contactList?.map { contact ->
            val socials = socialRepository.findSocialsByUserId(contact.saveUserId.users_id!!).map { socail ->
                Social(
                    socialPlatform = socail.socialPlatform,
                    socialLink = socail.socialLink
                )
            }
            val userRegistrations = registrationRepository.findRegistrationsByUserId(user.users_id!!).map {
                it.event.event_name to it.event.event_slug
            }
            val contactRegistrations = registrationRepository.findRegistrationsByUserId(contact.saveUserId.users_id!!).map {
                it.event.event_name to it.event.event_slug
            }
            val mutualEvents = userRegistrations.intersect(contactRegistrations).map { (eventName, eventSlug) ->
                MutualEvent(
                    eventName = eventName,
                    eventSlug = eventSlug
                )
            }
            var updateContactUser = contact.saveUserId
            if(contact.saveUserId.users_image !== null){
                updateContactUser = contact.saveUserId.copy(users_image = minioService.getImageUrl("profiles",updateContactUser.users_image!!))
            }
            ContactSavedDTO(
                contactId = contact.contactId!!,
                userProfile = updateContactUser,
                userSocials = socials,
                mutualEvents = mutualEvents
            )
        }
    }

    fun getContactsSortAndGroup(username: String): Map<Char, List<ContactSavedDTO>>? {
        val user = userRepository.findByUsername(username) ?: return null
        val contactList = contactRepository.findContactsByUserId(user.users_id!!)

        return contactList?.map { contact ->
            val socials = socialRepository.findSocialsByUserId(contact.saveUserId.users_id!!).map { socail ->
                Social(
                    socialPlatform = socail.socialPlatform,
                    socialLink = socail.socialLink
                )
            }
            val userRegistrations = registrationRepository.findRegistrationsByUserId(user.users_id!!).map {
                it.event.event_name to it.event.event_slug
            }
            val contactRegistrations = registrationRepository.findRegistrationsByUserId(contact.saveUserId.users_id!!).map {
                it.event.event_name to it.event.event_slug
            }
            val mutualEvents = userRegistrations.intersect(contactRegistrations).map { (eventName, eventSlug) ->
                MutualEvent(
                    eventName = eventName,
                    eventSlug = eventSlug
                )
            }
            var updateContactUser = contact.saveUserId
            if(contact.saveUserId.users_image !== null){
                updateContactUser = contact.saveUserId.copy(users_image = minioService.getImageUrl("profiles",updateContactUser.users_image!!))
            }
            ContactSavedDTO(
                contactId = contact.contactId!!,
                userProfile = updateContactUser,
                userSocials = socials,
                mutualEvents = mutualEvents
            )
        }?.sortedBy { it.userProfile.username.lowercase() }
            ?.groupBy { it.userProfile.username.first().uppercaseChar() }
    }

    fun getContactToken(username: String): String{
        val user = userRepository.findByUsername(username)
        val socials = socialRepository.findSocialsByUserId(user?.users_id!!)
        val additionalClaims = mapOf(
            "userId" to user.users_id,
            "username" to user.username,
            "name" to "${user.users_firstname} ${user.users_lastname}",
            "gender" to user.users_gender,
            "email" to user.users_email,
            "phone" to user.users_phone,
            "image" to user.users_image?.let { minioService.getImageUrl("profiles", it) },
            "birthday" to user.users_birthday.toString(),
            "age" to user.users_age,
            "socials" to socials
        )
        val expirationDate = Date(System.currentTimeMillis() + 1200000)
        val checkInToken = tokenService.generateCheckInToken(username, expirationDate, additionalClaims)
        return checkInToken
    }

    fun saveContact(username: String, tokenDTO: TokenDTO): ProfileDTO {
        val user = userRepository.findByUsername(username)
        val contactUserId = tokenService.getAllClaimsFromToken(tokenDTO.qrToken)!!["userId"] as Int
        val contactUsername = tokenService.getAllClaimsFromToken(tokenDTO.qrToken)!!["username"]
        val isContactExist = contactRepository.findContactByUserIdAndAndSaveUserId(user?.users_id!!, contactUserId.toLong()
        )
        if(isContactExist !== null){
            return userService.getUserProfileWithSocials(contactUsername.toString())
        }
        val contactUser = userRepository.findUserById(contactUserId.toLong())
        val savedContact = Contact(
            userId = user.users_id!!,
            saveUserId = contactUser!!
        )
        contactRepository.save(savedContact)
        return userService.getUserProfileWithSocials(contactUsername.toString())
    }

    fun deleteContact(username: String, contactId: Long) {
        try{
            val user = userRepository.findByUsername(username)
            val contactExist = contactRepository.findContactByUserIdAndContactId(user?.users_id!!, contactId)
                ?: throw EntityNotFoundException("Contact does not exist")
            contactRepository.delete(contactExist)
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }

    }
}