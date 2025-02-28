package com.gatherfy.gatherfyback.configuration

import com.gatherfy.gatherfyback.Exception.CustomUnauthorizedException
import com.gatherfy.gatherfyback.repositories.UserRepository
import com.gatherfy.gatherfyback.services.AuthService
import com.gatherfy.gatherfyback.services.TokenService
import com.gatherfy.gatherfyback.services.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenService: TokenService,
    private val userDetailsService: CustomUserDetailsService,
    private val authService: AuthService,
    private val userRepository: UserRepository
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ){
        try{
            val authHeader: String? = request.getHeader("Authorization")
            if (authHeader.doesNotContainBearerToken()) {
                chain.doFilter(request, response)
                return
            }
            val jwtToken = authHeader!!.extractTokenValue()
            val header = tokenService.getJwtHeader(jwtToken)

            val alg = header!!["alg"] as? String
            val username = if(alg.equals("RS256")){
                authService.identifyGoogleUser(jwtToken).toString()
            } else{
                tokenService.getUsernameFromToken(jwtToken)
            }

            if(username != null && SecurityContextHolder.getContext().authentication == null){
                val userExist = userRepository.findByUsername(username)
                if(userExist == null) {
                    // If user is not found
                    throw CustomUnauthorizedException("User do not have an account yet")
                }else{
                    val foundUser = userDetailsService.loadUserByUsername(username)

                    if(tokenService.isValidToken(jwtToken,foundUser)){
                        updateContext(foundUser, request)
                    }
                    chain.doFilter(request,response)
                }
            }
        }catch (e: CustomUnauthorizedException){
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized: ${e.message}")
            response.writer.flush()
        }
    }

    fun String?.doesNotContainBearerToken() =
        this == null || !this.startsWith("Bearer ")

    fun String.extractTokenValue() =
         this.substringAfter("Bearer ")

    fun updateContext(foundUser: UserDetails, request: HttpServletRequest) {
        val authToken = UsernamePasswordAuthenticationToken(foundUser, null, foundUser.authorities)
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authToken
    }
}