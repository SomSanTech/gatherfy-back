package com.gatherfy.gatherfyback.Exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomAccessDeniedHandler: AccessDeniedHandler {

    @Throws(IOException::class)
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException?
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.writer.write("Access Denied: You don't have permission to access this resource.")
    }
}