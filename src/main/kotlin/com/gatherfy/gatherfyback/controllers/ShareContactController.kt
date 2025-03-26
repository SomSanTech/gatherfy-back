package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.ContactSavedDTO
import com.gatherfy.gatherfyback.dtos.ProfileDTO
import com.gatherfy.gatherfyback.dtos.TokenDTO
import com.gatherfy.gatherfyback.services.ShareContactService
import com.gatherfy.gatherfyback.services.TokenService
import com.gatherfy.gatherfyback.services.UserService
import org.apache.coyote.BadRequestException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class ShareContactController(
    private val tokenService: TokenService,
    private val shareContactService: ShareContactService,
    private val userService: UserService
) {

    @GetMapping("/v1/contacts")
    fun getAllContacts(@RequestHeader("Authorization") token: String): List<ContactSavedDTO>?{
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        return shareContactService.getContacts(userId)
    }
    @GetMapping("/v2/contacts")
    fun getAllContactsWithSortAndGroup(@RequestHeader("Authorization") token: String): Map<Char, List<ContactSavedDTO>>? {
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        return shareContactService.getContactsSortAndGroup(userId)
    }
    @PostMapping("/v1/shareContact")
    fun getContactToken(@RequestHeader("Authorization") token: String): String{
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        return shareContactService.getContactToken(userId)
    }

    @PostMapping("/v1/saveContact")
    fun saveContact(
        @RequestHeader("Authorization") token: String,
        @RequestBody tokenDTO: TokenDTO
    ): ProfileDTO {
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        return shareContactService.saveContact(userId, tokenDTO)
    }

    @DeleteMapping("/v1/contact/{contactId}")
    fun deleteContact(
        @RequestHeader("Authorization") token: String,
        @PathVariable contactId: Long
    ): ResponseEntity<String>{
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        shareContactService.deleteContact(userId, contactId)
        return ResponseEntity.ok("Deleted contact successfully")
    }
}