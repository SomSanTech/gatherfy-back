package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.CreateSocialDTO
import com.gatherfy.gatherfyback.entities.Social
import com.gatherfy.gatherfyback.services.SocialService
import com.gatherfy.gatherfyback.services.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class SocialController(
    private val socialService: SocialService,
    private val tokenService: TokenService
) {
    @GetMapping("/v1/socials")
    fun getSocialLinks(@RequestHeader("Authorization") token: String): List<Social>{
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return socialService.getSocialLinks(userId.toLong())
    }

    @PutMapping("/v1/socials")
    fun createSocialLinks(
        @RequestHeader("Authorization") token: String,
        @RequestBody createSocialDTO: CreateSocialDTO
    ): ResponseEntity<String>{
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        socialService.updateSocialLink(userId.toLong(), createSocialDTO)
        return ResponseEntity.ok("Social links updated successfully")
    }
}