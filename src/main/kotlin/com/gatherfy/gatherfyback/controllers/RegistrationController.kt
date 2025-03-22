package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.RegistrationCheckin
import com.gatherfy.gatherfyback.services.RegistrationService
import com.gatherfy.gatherfyback.services.TokenService
import org.apache.coyote.BadRequestException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class RegistrationController(
    val registrationService: RegistrationService,
    private val tokenService: TokenService,
    @Qualifier("userDetailsService") private val userDetailsService: UserDetailsService
) {
    @GetMapping("/v1/registrations")
    fun getAllRegistrations():List<RegistrationDTO> {
        return registrationService.getAllRegistration()
    }
    @GetMapping("/v1/registrations/owner/{ownerId}")
    fun getRegistrationsByOwner(@PathVariable("ownerId") ownerId: String): List<RegistrationDTO>? {
        val registrationId = ownerId.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid registration ID format")
        return registrationService.getAllRegistrationsByOwner(registrationId)
    }
    @GetMapping("/v2/registrations")
    fun getRegistrationsByAuth(@RequestHeader("Authorization") token: String): List<RegistrationDTO>? {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return registrationService.getAllRegistrationsByOwnerAuth(username)
    }

    @GetMapping("/v1/registrations/{id}")
    fun getRegistrationById(@PathVariable("id") id: String): Optional<RegistrationDTO>? {
        val registrationId = id.toLongOrNull()
            ?: throw BadRequestException("Invalid registration ID format")
        return registrationService.getRegistrationById(registrationId)
    }

    @GetMapping("/v2/registrations/{id}")
    fun getRegistrationByIdWithAuth(
        @RequestHeader("Authorization") token: String,
        @PathVariable("id") id: String): RegistrationDTO {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        val registrationId = id.toLongOrNull()
            ?: throw BadRequestException("Invalid registration ID format")
        return registrationService.getRegistrationByIdWithAuth(username, registrationId)
    }

    @PutMapping("/v1/registrations/{id}")
    fun updateRegistrationStatus(
        @PathVariable("id") id: String,
        @RequestBody registrationDTO: RegistrationUpdateStatusDTO
    ): ResponseEntity<RegistrationDTO> {
        val registrationId = id.toLongOrNull()
            ?: throw BadRequestException("Invalid registration ID format")
        val updatedRegistration = registrationService.updateStatus(registrationId, registrationDTO.status)
        return ResponseEntity.ok(updatedRegistration)
    }

    @PutMapping("/v2/registrations/{id}")
    fun updateRegistrationStatusWithAuth(
        @RequestHeader("Authorization") token: String,
        @PathVariable("id") id: String,
        @RequestBody registrationDTO: RegistrationUpdateStatusDTO
    ): ResponseEntity<RegistrationDTO> {
        val registrationId = id.toLongOrNull()
            ?: throw BadRequestException("Invalid registration ID format")
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        val updatedRegistration = registrationService.updateStatusWithAuth(username, registrationId, registrationDTO.status)
        return ResponseEntity.ok(updatedRegistration)
    }

    @PostMapping("/v1/registrations")
    fun createRegistration(
        @RequestBody registrationCreateDTO: RegistrationCreateDTO
    ): ResponseEntity<RegistrationDTO> {
        val createdRegistration = registrationService.createRegistration(registrationCreateDTO)
        return ResponseEntity.ok(createdRegistration)
    }

    @PostMapping("/v2/registrations")
    fun createRegistrationWithAuth(
        @RequestHeader("Authorization") token: String,
        @RequestBody createRegistrationDTO: CreateRegistrationDTO
    ): RegistrationDTO {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return registrationService.createRegistrationWithAuth(username,createRegistrationDTO.eventId)
    }

    @GetMapping("/v1/registrations/event/{id}")
    fun getRegistrationEvents(@PathVariable("id") id: String): List<RegistrationDTO> {
        val eventId = id.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        return registrationService.getAllRegistrationsByEventId(eventId)
    }

    @GetMapping("/v2/registrations/event/{id}")
    fun getRegistrationEventsWithAuth(
        @RequestHeader("Authorization") token: String,
        @PathVariable("id") id: String): List<RegistrationDTO> {
        val eventId = id.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return registrationService.getAllRegistrationsByEventIdWithAuth(username,eventId)
    }

    @GetMapping("/v1/tickets")
    fun getRegistration(@RequestHeader("Authorization") token: String): List<UserRegistrationDTO>{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return registrationService.getRegistrationByUser(username)
    }

    @PostMapping("/v1/check-in/{eventId}")
    fun getCheckInToken(@RequestHeader("Authorization") token: String, @PathVariable eventId: Long): RegistrationCheckin{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return registrationService.getCheckInToken(username, eventId)
    }

    @PutMapping("/v1/check-in")
    fun checkedInAttendee(@RequestHeader("Authorization") qrToken: String):RegistrationCreateDTO{
        val token = qrToken.substringAfter("Bearer ")
        return registrationService.CheckedInAttendee(token)
    }

    @PutMapping("/v2/check-in")
    fun checkedInAttendee(@RequestHeader("Authorization") token: String, @RequestBody qrToken: TokenDTO):RegistrationCreateDTO{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return registrationService.CheckedInAttendeeWithAuth(username,qrToken)
    }
}