package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.CustomUnauthorizedException
import com.gatherfy.gatherfyback.entities.AuthRequest
import com.gatherfy.gatherfyback.entities.AuthResponse
import com.gatherfy.gatherfyback.properties.JwtProperties
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val userRepository: UserRepository,
) {

    fun authentication(authRequest: AuthRequest): AuthResponse {
        try{
            val users = userRepository.findUserByUsernameOrEmail(authRequest.username)
                ?: throw CustomUnauthorizedException("Username or email incorrect")
            val user = userDetailsService.loadUserByUsername(users.username)

            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    users.username,
                    authRequest.password
                )
            )

            val additionalClaims = mapOf(
                "role" to users.users_role
            )
            val accessToken = tokenService.generateToken(users.users_id,getAccessTokenExpiration(),additionalClaims)
            val refreshToken = tokenService.generateRefreshToken(users.users_id,getRefreshTokenExpiration(),additionalClaims)
            return AuthResponse(accessToken, refreshToken)
        } catch (e: CustomUnauthorizedException ){
            throw CustomUnauthorizedException(e.message!!)
        }
    }

    fun authenticationGoogle(token: String): Any {
        try{
            val accountEmail = tokenService.getAllClaimsFromToken(token)!!["email"]
            val accountExist = userRepository.findByEmail(accountEmail.toString())
            println("accountExist $accountExist")

            if(accountExist !== null){
                val user = userDetailsService.loadUserByUsername(accountExist.username)

                println("user = ${accountExist.username}")

                val additionalClaims = mapOf(
                    "role" to accountExist.users_role
                )
                val accessToken = tokenService.generateToken(accountExist.users_id,getAccessTokenExpiration(),additionalClaims)
                val refreshToken = tokenService.generateRefreshToken(accountExist.users_id,getRefreshTokenExpiration(),additionalClaims)
                return AuthResponse(accessToken, refreshToken)
            } else{
                throw CustomUnauthorizedException("User do not have an account yet")
            }
        } catch (e: CustomUnauthorizedException ){
            throw CustomUnauthorizedException(e.message!!)
        }
    }

    fun getAccessTokenExpiration():Date{
        return Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
    }
    fun getRefreshTokenExpiration():Date{
        return Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration)
    }

    fun identifyGoogleUser(token: String): Long?{
        val accountEmail = tokenService.getAllClaimsFromToken(token)!!["email"]
        val accountExist = userRepository.findByEmail(accountEmail.toString())
        if(accountExist !== null){
            return accountExist.users_id
        }
        return null
    }

}