package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.AuthRequest
import com.gatherfy.gatherfyback.entities.AuthResponse
import com.gatherfy.gatherfyback.properties.JwtProperties
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.Date

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val userRepository: UserRepository
) {

    fun authentication(authRequest: AuthRequest): AuthResponse {
        try{
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    authRequest.username,
                    authRequest.password
                )
            )
            val user = userDetailsService.loadUserByUsername(authRequest.username)
            val users = userRepository.findByUsername(user.username)
            val additionalClaims = mapOf(
                "role" to users?.users_role
            )
            val accessToken = tokenService.generateToken(user,getAccessTokenExpiration(),additionalClaims)
            val refreshToken = tokenService.generateRefreshToken(user,getRefreshTokenExpiration(),additionalClaims)
            return AuthResponse(accessToken, refreshToken)
        } catch (e: ResponseStatusException ){
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    fun getAccessTokenExpiration():Date{
        return Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
    }
    fun getRefreshTokenExpiration():Date{
        return Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration)
    }
}