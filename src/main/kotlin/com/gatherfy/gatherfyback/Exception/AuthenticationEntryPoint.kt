package com.gatherfy.gatherfyback.Exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class AuthenticationEntryPoint: AuthenticationEntryPoint {
    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write("Unauthorized: You need to log in to access this resource.")
    }
}