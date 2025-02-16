package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.AccessDeniedException
import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.Exception.CustomUnauthorizedException
import com.gatherfy.gatherfyback.dtos.CheckInDTO
import com.gatherfy.gatherfyback.dtos.RegistrationCreateDTO
import com.gatherfy.gatherfyback.dtos.RegistrationDTO
import com.gatherfy.gatherfyback.dtos.UserRegistrationDTO
import com.gatherfy.gatherfyback.entities.Registration
import com.gatherfy.gatherfyback.entities.RegistrationCheckin
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.RegistrationRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.checkerframework.checker.units.qual.A
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
    @Qualifier("userDetailsService") private val userDetailsService: UserDetailsService,
    private val emailSenderService: EmailSenderService
){
    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun getAllRegistration(): List<RegistrationDTO> {
        val registrations = registrationRepository.findAll()
        return registrations.map { toRegistrationDTO(it) }
    }

    fun getAllRegistrationsByEventId(eventId: Long): List<RegistrationDTO> {
        try{
            val registrations = registrationRepository.findRegistrationsByEventId(eventId)
            return registrations.map { toRegistrationDTO(it) }
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun getAllRegistrationsByEventIdWithAuth(username: String, eventId: Long): List<RegistrationDTO> {
        try{
            val user = userRepository.findByUsername(username)
            val isOwnerEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id!!, eventId)
                ?: throw EntityNotFoundException("Event id $eventId does not exist")
            val registrations = registrationRepository.findRegistrationsByEventId(eventId)
            return registrations.map { toRegistrationDTO(it) }
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun getAllRegistrationsByOwner(ownerId: Long): List<RegistrationDTO>? {
        val registrations = registrationRepository.findRegistrationsByEventOwner(ownerId)
        return registrations?.map { toRegistrationDTO(it) }
    }

    fun getAllRegistrationsByOwnerAuth(username: String): List<RegistrationDTO>? {
        val user = userRepository.findByUsername(username)
        val registrations = registrationRepository.findRegistrationsByEventOwner(user?.users_id!!)
        return registrations?.map { toRegistrationDTO(it) }
    }

    fun getRegistrationById(registrationId: Long): Optional<RegistrationDTO>? {
        try{
            val registration = registrationRepository.findById(registrationId)
            if (registration.isEmpty) {
                throw EntityNotFoundException("Registration id $registrationId does not exist")
            }
            return registration.map { toRegistrationDTO(it) }
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun getRegistrationByIdWithAuth(username: String,registrationId: Long): RegistrationDTO {
        try{
            val user = userRepository.findByUsername(username)
            val registration = registrationRepository.findByRegistrationId(registrationId)
                ?: throw EntityNotFoundException("Registration id $registrationId does not exist")
            val isOwnerEvent = registrationRepository.findByOwnerIdAndEventId(user?.users_id!!, registration.eventId, registrationId)
                ?: throw AccessDeniedException("You are not owner of this event")

            return  toRegistrationDTO(registration)

        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }

    fun updateStatus(id: Long, newStatus: String): RegistrationDTO {
        try{
            val registration = registrationRepository.findById(id)
                .orElseThrow {  EntityNotFoundException("Registration id $id does not exist") }

            registration.status = newStatus
            val updatedRegistration = registrationRepository.save(registration)

            return toRegistrationDTO(updatedRegistration)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: Exception){
            throw BadRequestException("Invalid status provided. Allowed values are: 'Awaiting Check-in', 'Checked in', or 'Unattended'.")
        }
    }

    fun updateStatusWithAuth(username: String, id: Long, newStatus: String): RegistrationDTO {
        try{
            val user = userRepository.findByUsername(username)
            val registration = registrationRepository.findById(id)
                .orElseThrow {  EntityNotFoundException("Registration id $id does not exist") }
            val isOwnerEvent = registrationRepository.findByOwnerIdAndEventId(user?.users_id!!, registration.eventId, id)
                ?: throw AccessDeniedException("You are not owner of this event")
            registration.status = newStatus
            val updatedRegistration = registrationRepository.save(registration)

            return toRegistrationDTO(updatedRegistration)
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        } catch (e: Exception){
            throw BadRequestException("Invalid status provided. Allowed values are: 'Awaiting Check-in', 'Checked in', or 'Unattended'.")
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

    fun createRegistrationWithAuth(username: String, eventId: Long): RegistrationDTO {
        try {
            val user = userRepository.findByUsername(username)
            val event = eventRepository.findById(eventId)
                .orElseThrow { EntityNotFoundException("Event id $eventId does not exist") }

            val countRegistration = registrationRepository.findRegistrationsByEventId(eventId).count()
            if(countRegistration == event.event_capacity.toInt()){
                throw ConflictException("This event has reached full capacity")
            }

            // Check for existing registration to prevent duplicates
            val existingRegistration = registrationRepository.findByEventIdAndUserId(
                eventId,
                user?.users_id!!
            )
            if (existingRegistration != null) {
                throw ConflictException("User is already registered for this event")
            }

            val registration = Registration(
                event = event,
                user = user,
                status = "Awaiting Check-in",
                createdAt = ZonedDateTime.now(),
                eventId = eventId,
                userId = user.users_id!!
            )

            val savedRegistration = registrationRepository.save(registration)
            emailSenderService.sendRegistrationConfirmation(event,user)
            val updateEventRegistration = countRegistration + 1
            if(updateEventRegistration == event.event_capacity.toInt()){
                event.event_status = "full"
                eventRepository.save(event)
            }
            return toRegistrationDTO(savedRegistration)
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: ConflictException) {
            throw ConflictException(e.message!!)
        } catch (e: Exception){
            throw BadRequestException("Invalid status provided. Allowed values are: 'Awaiting Check-in', 'Checked in', or 'Unattended'.")
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
        try{
            val user = userRepository.findByUsername(username)
            val isEventExist = eventRepository.findById(eventId)
            if(isEventExist.isEmpty){
                throw EntityNotFoundException("Event id $eventId does not exist")
            }
            val isRegistration = registrationRepository.findRegistrationsByUserIdAndEventId(user?.users_id!!.toInt(), eventId.toInt())
            if(isRegistration === null){
                throw EntityNotFoundException("User not register to this event")
            }
            val additionalClaims = mapOf(
                "userId" to user.users_id,
                "eventId" to eventId
            )
            val expirationDate = Date(System.currentTimeMillis() + 600000)
            val checkInToken = tokenService.generateCheckInToken(username, expirationDate, additionalClaims)
            return RegistrationCheckin(checkInToken)
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun CheckedInAttendee(token: String): RegistrationCreateDTO{
        val isTokenExpired = tokenService.isTokenExpired(token)
        if (isTokenExpired) {
            throw CustomUnauthorizedException("Check-in token has expired. Please generate a new token.")
        }
        val userId = (tokenService.getAdditionalClaims(token, "userId")) as Int
        val eventId = (tokenService.getAdditionalClaims(token, "eventId")) as Int
        val registration = registrationRepository.findRegistrationsByUserIdAndEventId(userId,eventId)
        registration?.status = "Checked In"
        val updatedRegistration = registrationRepository.save(registration!!)
        return toCheckedInDto(updatedRegistration)
    }

    fun CheckedInAttendeeWithAuth(username: String, checkInDto: CheckInDTO): RegistrationCreateDTO{
        try{
            val user = userRepository.findByUsername(username)
            val userId = (tokenService.getAdditionalClaims(checkInDto.qrToken, "userId")) as Int
            val eventId = (tokenService.getAdditionalClaims(checkInDto.qrToken, "eventId")) as Int
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, eventId.toLong())

            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            val registration = registrationRepository.findRegistrationsByUserIdAndEventId(userId,eventId)

            registration?.status = "Checked In"
            val updatedRegistration = registrationRepository.save(registration!!)
            return toCheckedInDto(updatedRegistration)
        }catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }

    fun toCheckedInDto(registration: Registration): RegistrationCreateDTO{
        return RegistrationCreateDTO(
            userId = registration.userId,
            eventId = registration.eventId,
            status = registration.status
        )
    }
}