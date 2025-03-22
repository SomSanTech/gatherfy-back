package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.entities.AuthRequest
import com.gatherfy.gatherfyback.entities.AuthResponse
import com.gatherfy.gatherfyback.repositories.UserRepository
import com.gatherfy.gatherfyback.services.AuthService
import com.gatherfy.gatherfyback.services.CustomUserDetailsService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/", "http://localhost:3000/"])
class AuthController(
    private val authService: AuthService,
    private val tokenService: TokenService,
    private val userDetailsService: CustomUserDetailsService,
    private val userRepository: UserRepository
) {
    @PostMapping("/v1/login")
    fun createAuthenticationToken(
        @RequestBody authRequest: AuthRequest,
        response: HttpServletResponse
    ): AuthResponse {
        val authResponse = authService.authentication(authRequest)

//        val refreshCookie = Cookie("refresh_token", authResponse.refreshToken).apply {
//            isHttpOnly = true  // ✅ ป้องกัน XSS
//            secure = true      // ✅ ใช้เฉพาะ HTTPS
//            path = "/"         // ✅ ใช้ได้กับทุกหน้า
//            maxAge = 7 * 24 * 60 * 60  // ✅ 7 วัน
//        }
//
//        response.addCookie(refreshCookie)
        return authResponse
    }

    @PostMapping("/v1/login/google")
    fun createAuthenticationGoogle(
        @RequestHeader("Authorization") token: String,
    ): Any {
        return authService.authenticationGoogle(token.substringAfter("Bearer "))
    }

//    @PostMapping("/v1/refresh")
//    fun refreshToken(request: HttpServletRequest): Any {
//        val refreshToken = request.getHeader("Authorization").replace("Bearer ", "")
//        println(refreshToken)
//        if (refreshToken.isNullOrBlank()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is missing")
//        }
//        val username = tokenService.getUsernameFromToken(refreshToken)
//
//        val foundUser = userDetailsService.loadUserByUsername(username)
//        val user = userDetailsService.loadUserByUsername(username)
//        val users = userRepository.findByUsername(user.username)
//        val additionalClaims = mapOf(
//            "role" to users?.users_role
//        )
//        val accessToken = tokenService.generateToken(user, authService.getAccessTokenExpiration(), additionalClaims)
//
//        if (!tokenService.isValidToken(refreshToken, foundUser)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token")
//        }
//
//        return accessToken
//
//    }

    @PostMapping("/refresh")
    fun refresh(@CookieValue("refreshToken") refreshToken: String?,request: HttpServletRequest, response: HttpServletResponse?): ResponseEntity<*> {
        println(refreshToken)
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                println("Cookie name: ${cookie.name}, Cookie value: ${cookie.value}")
            }
        } else {
            println("No cookies found.")
        }

        println(cookies)
        if (refreshToken != null) {
//            for (cookie in cookies) {
//                if ("refresh_token" == cookie.name) {
//                    println("Cookie name: ${cookie.name}, Cookie value: ${cookie.value}")
//                    println("refresh")
//                    println(cookie.value)
//                    val refreshToken = cookie.value

                    val username = tokenService.getUsernameFromToken(refreshToken)
                    val user = userDetailsService.loadUserByUsername(username)
                    val users = userRepository.findByUsername(user.username)
                    val additionalClaims = mapOf(
                        "role" to users?.users_role
                    )
                    val foundUser = userDetailsService.loadUserByUsername(username)
                    if (tokenService.isValidToken(refreshToken, foundUser)) {
                        val newAccessToken: String =
                            tokenService.generateToken(user, authService.getAccessTokenExpiration(), additionalClaims)
                        return ResponseEntity.ok(mapOf("accessToken" to newAccessToken))
                    }
//                }
//            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token")
    }

}