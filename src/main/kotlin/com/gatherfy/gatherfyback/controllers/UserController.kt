package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.CreateUserDTO
import com.gatherfy.gatherfyback.dtos.UserDTO
import com.gatherfy.gatherfyback.services.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class UserController(private val userService: UserService) {

    @PostMapping("/v1/signup")
    fun createUser(@RequestBody user: CreateUserDTO): UserDTO{
        return userService.createUser(user)
    }
}