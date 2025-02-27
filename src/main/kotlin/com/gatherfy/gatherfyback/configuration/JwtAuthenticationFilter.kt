package com.gatherfy.gatherfyback.configuration

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
    private val userDetailsService: CustomUserDetailsService
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ){
        val authHeader: String? = request.getHeader("Authorization")
        if (authHeader.doesNotContainBearerToken()) {
            chain.doFilter(request, response)
            return
        }
        val jwtToken = authHeader!!.extractTokenValue()
        val username = tokenService.getUsernameFromToken(jwtToken)
        if(username != null && SecurityContextHolder.getContext().authentication == null){
            val foundUser = userDetailsService.loadUserByUsername(username)
            if(tokenService.isValidToken(jwtToken,foundUser)){
                updateContext(foundUser, request)
            }
            chain.doFilter(request,response)
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