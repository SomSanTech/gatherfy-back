package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.entities.AuthRequest
import com.gatherfy.gatherfyback.entities.AuthResponse
import com.gatherfy.gatherfyback.services.AuthService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    fun createAuthenticationToken(@RequestBody authRequest: AuthRequest): AuthResponse{
        return authService.authentication(authRequest)
    }
}