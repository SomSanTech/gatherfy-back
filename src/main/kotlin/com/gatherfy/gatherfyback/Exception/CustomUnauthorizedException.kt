package com.gatherfy.gatherfyback.Exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class CustomUnauthorizedException(message: String): RuntimeException(message) {
}