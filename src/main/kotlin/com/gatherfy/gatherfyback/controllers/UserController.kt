package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.OTPVerificationRequest
import com.gatherfy.gatherfyback.entities.ResendOTPRequest
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.services.TokenService
import com.gatherfy.gatherfyback.services.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
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
    fun createUser(@Valid @RequestBody user: CreateUserDTO): UserDTO{
        return userService.createUser(user)
    }

    @PostMapping("/v1/verify-otp")
    fun verifyOTP(@RequestBody otpVerificationRequest: OTPVerificationRequest): ResponseEntity<String> {
        return userService.verifyOTP(otpVerificationRequest)
    }

    @PostMapping("/v1/resend-otp")
    fun resendOTP(@RequestBody resendOTPRequest: ResendOTPRequest): ResponseEntity<String>{
        return userService.resendOTP(resendOTPRequest)
    }

    @GetMapping("/v1/profile")
    fun getProfile(@RequestHeader("Authorization") token: String): User? {
        return userService.getUserProfile(tokenService.getUsernameFromToken(token.substringAfter("Bearer ")))
    }

    @PutMapping("/v1/profile")
    fun editProfile(@RequestHeader("Authorization") token: String,@Valid @RequestBody userEdit: EditUserDTO): User? {
        return userService.updateUser(tokenService.getUsernameFromToken(token.substringAfter("Bearer ")),userEdit)
    }

    @PostMapping("/v1/signup/google")
    fun createAuthenticationGoogle(
        @RequestBody createUserGoogle: CreateUserGoogleDTO
    ): UserDTO {
        return userService.createUserFromGoogle(createUserGoogle)
    }

    @PutMapping("/v1/password")
    fun editPassword(@RequestHeader("Authorization") token: String,@RequestBody editPasswordDTO: EditPasswordDTO): ResponseEntity<String> {
        return userService.updatePassword(tokenService.getUsernameFromToken(token.substringAfter("Bearer ")),editPasswordDTO)
    }
}