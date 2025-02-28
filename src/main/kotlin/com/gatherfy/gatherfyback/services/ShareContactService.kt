package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.TokenDTO
import com.gatherfy.gatherfyback.entities.Contact
import com.gatherfy.gatherfyback.repositories.ContactRepository
import com.gatherfy.gatherfyback.repositories.SocialRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ShareContactService(
    private val userRepository: UserRepository,
    private val socialRepository: SocialRepository,
    private val tokenService: TokenService,
    private val minioService: MinioService,
    private val contactRepository: ContactRepository
) {

    fun getContacts(username: String): List<Contact>? {
        val user = userRepository.findByUsername(username)
        val contacts = contactRepository.findContactsByUserId(user?.users_id!!)
        return contacts
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
            "image" to minioService.getImageUrl("profiles",user.users_image!!) ,
            "birthday" to user.users_birthday.toString(),
            "age" to user.users_age,
            "socials" to socials
        )
        val expirationDate = Date(System.currentTimeMillis() + 600000)
        val checkInToken = tokenService.generateCheckInToken(username, expirationDate, additionalClaims)
        return checkInToken
    }

    fun saveContact(username: String, tokenDTO: TokenDTO) {
        val user = userRepository.findByUsername(username)
        val contactUser = tokenService.getAllClaimsFromToken(tokenDTO.qrToken)!!["userId"]
        val savedContact = Contact(
            userId = user?.users_id!!,
            saveUserId = contactUser as Int
        )
        contactRepository.save(savedContact)
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