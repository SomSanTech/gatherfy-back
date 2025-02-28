package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.TokenDTO
import com.gatherfy.gatherfyback.entities.Contact
import com.gatherfy.gatherfyback.services.ShareContactService
import com.gatherfy.gatherfyback.services.TokenService
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
    private val shareContactService: ShareContactService
) {

    @GetMapping("/v1/contacts")
    fun getAllContacts(@RequestHeader("Authorization") token: String): List<Contact>?{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return shareContactService.getContacts(username)
    }
    @PostMapping("/v1/shareContact")
    fun getContactToken(@RequestHeader("Authorization") token: String): String{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return shareContactService.getContactToken(username)
    }

    @PostMapping("/v1/saveContact")
    fun saveContact(
        @RequestHeader("Authorization") token: String,
        @RequestBody tokenDTO: TokenDTO
    ): ResponseEntity<String> {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        shareContactService.saveContact(username, tokenDTO)
        return ResponseEntity.ok("Saved contact successfully")
    }

    @DeleteMapping("/v1/contact/{contactId}")
    fun deleteContact(
        @RequestHeader("Authorization") token: String,
        @PathVariable contactId: Long
    ): ResponseEntity<String>{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        shareContactService.deleteContact(username, contactId)
        return ResponseEntity.ok("Deleted contact successfully")
    }
}