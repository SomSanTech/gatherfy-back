package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.RegistrationCreateDTO
import com.gatherfy.gatherfyback.dtos.RegistrationDTO
import com.gatherfy.gatherfyback.dtos.UserRegistrationDTO
import com.gatherfy.gatherfyback.entities.Registration
import com.gatherfy.gatherfyback.entities.RegistrationCheckin
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.RegistrationRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

@Service
class RegistrationService(
    val registrationRepository: RegistrationRepository,
    val eventRepository: EventRepository,
    val userRepository: UserRepository,
    private val tokenService: TokenService,
    @Qualifier("userDetailsService") private val userDetailsService: UserDetailsService
){
    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun getAllRegistration(): List<RegistrationDTO> {
        val registrations = registrationRepository.findAll()
        return registrations.map { toRegistrationDTO(it) }
    }

    fun getAllRegistrationsByEventId(eventId: Long): List<RegistrationDTO> {
        val registrations = registrationRepository.findRegistrationsByEventId(eventId)
        if (registrations.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found")
        }
        return registrations.map { toRegistrationDTO(it) }
    }

    fun getAllRegistrationsByOwner(ownerId: Long): List<RegistrationDTO> {
        val registrations = registrationRepository.findRegistrationsByEventOwner(ownerId)
        if (registrations.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found")
        }
        return registrations.map { toRegistrationDTO(it) }
    }

    fun getRegistrationById(registrationId: Long): Optional<RegistrationDTO> {
        val registration = registrationRepository.findById(registrationId)
        if (registration.isEmpty) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found")
        }
        return registration.map { toRegistrationDTO(it) }
    }

    fun updateStatus(id: Long, newStatus: String): RegistrationDTO {
        try{
            val registration = registrationRepository.findById(id)
                .orElseThrow {  ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found") }

            registration.status = newStatus
            val updatedRegistration = registrationRepository.save(registration)

            return toRegistrationDTO(updatedRegistration)
        } catch (e: ResponseStatusException){
            throw e
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun createRegistration(registrationCreateDTO: RegistrationCreateDTO): RegistrationDTO {
        try {
            val event = eventRepository.findById(registrationCreateDTO.eventId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found") }

            val user = userRepository.findById(registrationCreateDTO.userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND,"User not found") }

            // Check for existing registration to prevent duplicates
            val existingRegistration = registrationRepository.findByEventIdAndUserId(
                registrationCreateDTO.eventId,
                registrationCreateDTO.userId
            )
            if (existingRegistration != null) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already registered for this event"
                )
            }

            val registration = Registration(
                event = event,
                user = user,
                status = registrationCreateDTO.status,
                createdAt = ZonedDateTime.now(),
                eventId = registrationCreateDTO.eventId,
                userId = registrationCreateDTO.userId
            )

            val savedRegistration = registrationRepository.save(registration)
            return toRegistrationDTO(savedRegistration)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    private fun toRegistrationDTO(registration: Registration): RegistrationDTO {
        return RegistrationDTO(
            registrationId = registration.registrationId,
            eventName = registration.event.event_name,
            firstName = registration.user.users_firstname,
            lastName = registration.user.users_lastname,
            username = registration.user.username,
            gender = registration.user.users_gender,
            dateOfBirth = registration.user.users_birthday,
            age = registration.user.users_age,
            email = registration.user.users_email,
            phone = registration.user.users_phone,
            status = registration.status,
            createdAt = registration.createdAt
        )
    }

    fun getRegistrationByUser(username: String): List<UserRegistrationDTO>{
        try{
            val user = userRepository.findByUsername(username)
            val registrations = user?.users_id?.let { registrationRepository.findRegistrationsByUserId(it) }
                return registrations!!.map { registration -> toUserRegistrationDto(registration) }
        }
        catch (e: Exception){
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    private fun toUserRegistrationDto(registration: Registration): UserRegistrationDTO{
        val ownerEventName: String = userRepository.findById(registration.event.event_owner).map {
            it.username
        }.orElse("Unknown Organizer")
        return UserRegistrationDTO(
            registrationId = registration.registrationId,
            eventId = registration.eventId,
            name = registration.event.event_name,
            description = registration.event.event_desc,
            detail = registration.event.event_detail,
            start_date = registration.event.event_start_date,
            end_date = registration.event.event_end_date,
            location = registration.event.event_location,
            status = registration.event.event_status,
            slug = registration.event.event_slug,
            image = getImageUrl("thumbnails", registration.event.event_image),
            owner = ownerEventName,
            tags = registration.event.tags?.map { it.tag_title }
        )
    }
    fun getImageUrl(bucketName: String, objectName: String): String {
        return "$minioDomain/$bucketName/$objectName"
    }

    fun getCheckInToken(username: String, eventId: Long): RegistrationCheckin{
        val user = userRepository.findByUsername(username)
        val additionalClaims = mapOf(
            "userId" to user!!.users_id,
            "eventId" to eventId
        )
        val expirationDate = Date(System.currentTimeMillis() + 600000)
        val checkInToken = tokenService.generateCheckInToken(username, expirationDate, additionalClaims)
        return RegistrationCheckin(checkInToken)
    }

    fun CheckedInAttendee(token: String): RegistrationCreateDTO{
        val userId = (tokenService.getAdditionalClaims(token, "userId")) as Int
        val eventId = (tokenService.getAdditionalClaims(token, "eventId")) as Int
        val registration = registrationRepository.findRegistrationsByUserIdAndEventId(userId,eventId)
        registration.status = "Checked In"
        val updatedRegistration = registrationRepository.save(registration)
        return toCheckedInDto(updatedRegistration)
    }

    fun toCheckedInDto(registration: Registration): RegistrationCreateDTO{
        return RegistrationCreateDTO(
            userId = registration.userId,
            eventId = registration.eventId,
            status = registration.status
        )
    }
}