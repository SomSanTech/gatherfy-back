package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.CreateSocialDTO
import com.gatherfy.gatherfyback.entities.Social
import com.gatherfy.gatherfyback.services.SocialService
import com.gatherfy.gatherfyback.services.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class SocialController(
    private val socialService: SocialService,
    private val tokenService: TokenService
) {
    @GetMapping("/v1/socials")
    fun getSocialLinks(@RequestHeader("Authorization") token: String): List<Social>{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return socialService.getSocialLinks(username)
    }

    @PostMapping("/v1/socials")
    fun createSocialLinks(
        @RequestHeader("Authorization") token: String,
        @RequestBody createSocialDTO: CreateSocialDTO
    ): ResponseEntity<String>{
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        socialService.createSocialLink(username, createSocialDTO)
        return ResponseEntity.ok("Social links updated successfully")

    }
}