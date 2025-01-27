package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.CreateUserDTO
import com.gatherfy.gatherfyback.dtos.EditUserDTO
import com.gatherfy.gatherfyback.dtos.UserDTO
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.services.TokenService
import com.gatherfy.gatherfyback.services.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class UserController(
    private val userService: UserService,
    private val tokenService: TokenService
) {

    @PostMapping("/v1/signup")
    fun createUser(@RequestBody user: CreateUserDTO): UserDTO{
        return userService.createUser(user)
    }

    @GetMapping("/v1/profile")
    fun getProfile(@RequestHeader("Authorization") token: String): User? {
        return userService.getUserProfile(tokenService.getUsernameFromToken(token.substringAfter("Bearer ")))
    }

    @PutMapping("/v2/profile")
    fun editProfile(@RequestHeader("Authorization") token: String, @RequestBody userEdit: EditUserDTO): User? {
        return userService.updateUser(tokenService.getUsernameFromToken(token.substringAfter("Bearer ")),userEdit)
    }
}